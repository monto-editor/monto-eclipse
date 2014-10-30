package de.tudarmstadt.stg.monto.connection;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.zeromq.ZMQ.Context;

public class Connection {

	public static boolean hasTimedOut(Object ob) {
		return ob == null;
	}
	
	public static Reader configFile() throws IOException {
		String home = System.getProperty("user.home");
		Path montoConfig = FileSystems.getDefault().getPath(home, ".monto");
		return Files.newBufferedReader(montoConfig);
	}

	public static SourceConnection createSourceConnection(Context context) throws ConnectionParseException {
		try {
			JSONObject montoConfig = (JSONObject) JSONValue.parse(configFile());
			JSONObject connectionInfo = (JSONObject) montoConfig.get("connection");
			String fromSource = (String) connectionInfo.get("from_source");
			return new SourceConnection(new OutgoingConnection(context, fromSource));
		} catch (Exception e) {
			throw new ConnectionParseException(e);
		}
	}
	
	public static SinkConnection createSinkConnection(Context context) throws ConnectionParseException {
		try {
			JSONObject montoConfig = (JSONObject) JSONValue.parse(configFile());
			JSONObject connectionInfo = (JSONObject) montoConfig.get("connection");
			String toSinks = (String) connectionInfo.get("to_sinks");
			return new SinkConnection(new IncommingConnection(context, toSinks));
		} catch (Exception e) {
			throw new ConnectionParseException(e);
		}
	}
	
	public static ServerConnection createServerConnection(Context context) throws ConnectionParseException {
		try {
			JSONObject montoConfig = (JSONObject) JSONValue.parse(configFile());
			JSONObject connectionInfo = (JSONObject) montoConfig.get("connection");
			String toServer = (String) connectionInfo.get("to_server");
			String fromServer = (String) connectionInfo.get("from_server");
			return new ServerConnection(
					new IncommingConnection(context, toServer),
					new OutgoingConnection(context, fromServer));
		} catch (Exception e) {
			throw new ConnectionParseException(e);
		}
	}
}
