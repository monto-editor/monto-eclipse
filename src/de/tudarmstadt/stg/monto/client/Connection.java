package de.tudarmstadt.stg.monto.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessageParseException;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class Connection implements AutoCloseable {
	
	private String fromSource;
	private String toSinks;
	private int numThreads;
	
	private Context context;
	private Socket fromSourceSocket = null;
	private Socket toSinksSocket = null;

	public Connection(String fromSource, String toSinks, int ioThreads) {
		this.fromSource = fromSource;
		this.toSinks = toSinks;
		this.numThreads = ioThreads;
	}

	public String getFromSource() {
		return fromSource;
	}
	
	public String getToSinks() {
		return toSinks;
	}
	
	public int getNumberOfThreads() {
		return numThreads;
	}

	public static Connection create(Reader reader) throws ConnectionParseException {
		try {
			JSONObject montoConfig = (JSONObject) JSONValue.parse(reader);
			JSONObject connectionInfo = (JSONObject) montoConfig.get("connection");
			String fromSource = (String) connectionInfo.get("from_source");
			String toSinks = (String) connectionInfo.get("to_sinks");
			int ioThreads = ((Long) connectionInfo.get("threads")).intValue();
			return new Connection(fromSource,toSinks,ioThreads);
		} catch (Exception e) {
			throw new ConnectionParseException(e);
		}
	}

	public void open() throws IOException {
		context = ZMQ.context(getNumberOfThreads());
		
		fromSourceSocket = context.socket(ZMQ.REQ);
		fromSourceSocket.connect(getFromSource());
		
		toSinksSocket = context.socket(ZMQ.SUB);
		toSinksSocket.connect(getToSinks());
	}
	
	@Override
	public void close() throws IOException {
		fromSourceSocket.close();
		toSinksSocket.close();
		context.term();
	}

	public void sendVersionMessage(VersionMessage message) {
		fromSourceSocket.send(VersionMessage.encode(message).toJSONString());
	}
	
	public ProductMessage receiveProductMessage() throws ProductMessageParseException {
		return ProductMessage.decode(new StringReader(toSinksSocket.recvStr()));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("from source: %s\n", getFromSource()));
		builder.append(String.format("to sinks:    %s\n", getToSinks()));
		return builder.toString();
	}
}
