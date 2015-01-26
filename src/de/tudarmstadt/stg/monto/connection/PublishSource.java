package de.tudarmstadt.stg.monto.connection;

import org.json.simple.JSONObject;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.message.VersionMessages;

public class PublishSource {
	private Publish connection;
	
	public PublishSource(Publish connection) {
		this.connection = connection;
	}
	
	public void connect() {
		connection.connect();
	}
	
	public void sendMessage(VersionMessage message) {
		try {
			JSONObject encoding = VersionMessages.encode(message);
			connection.sendMessage(encoding.toJSONString());
		} catch (Exception e) {
			Activator.error(e);
		}
	}


	public void close() {
		connection.close();
	}
}
