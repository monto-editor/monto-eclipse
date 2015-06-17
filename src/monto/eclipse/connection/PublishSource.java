package monto.eclipse.connection;

import monto.eclipse.Activator;
import monto.eclipse.message.VersionMessage;
import monto.eclipse.message.VersionMessages;

import org.json.simple.JSONObject;

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
