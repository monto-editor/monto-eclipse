package de.tudarmstadt.stg.monto.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

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
	
	public static Connection create() throws ConnectionParseException, IOException {
		String home = System.getProperty("user.home");
		Path montoConfig = FileSystems.getDefault().getPath(home, ".monto");
		Reader reader = Files.newBufferedReader(montoConfig);
		return create(reader);	
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
		openFromSourceSocket();
		opentoSinksSocket();
	}
	
	private void openFromSourceSocket() {
		fromSourceSocket = context.socket(ZMQ.REQ);
		fromSourceSocket.setReceiveTimeOut(seconds(2));
		fromSourceSocket.setLinger(seconds(2));
		fromSourceSocket.connect(getFromSource());
	}
	
	private void opentoSinksSocket() {
		toSinksSocket = context.socket(ZMQ.SUB);
		toSinksSocket.connect(getToSinks());
		toSinksSocket.subscribe(new byte[]{});
		toSinksSocket.setReceiveTimeOut(seconds(2));
	}

	@Override
	public void close() throws IOException {
		fromSourceSocket.close();
		toSinksSocket.close();
		context.term();
	}

	public void sendVersionMessage(VersionMessage message) {
		fromSourceSocket.send(VersionMessage.encode(message).toJSONString());
		byte[] ack = fromSourceSocket.recv();
		
		if(hasTimedOut(ack)) {
			
			// Close and reopen connection to be able to send messages the next time.
			fromSourceSocket.close();
			openFromSourceSocket();
		}
		
	}

	/**
	 * Either blocks under 2 seconds until a product message arrives
	 * or returns null if the connection has timed out.
	 */
	public ProductMessage receiveProductMessage() throws ProductMessageParseException {
		String response = toSinksSocket.recvStr();
		if(hasTimedOut(response))
			return null;
		else
			return ProductMessage.decode(new StringReader(response));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("from source: %s\n", getFromSource()));
		builder.append(String.format("to sinks:    %s\n", getToSinks()));
		return builder.toString();
	}
	
	
	// Helper Methods

	private static int seconds(int s) {
		return s * 1000;
	}

	private static boolean hasTimedOut(Object ob) {
		return ob == null;
	}
}
