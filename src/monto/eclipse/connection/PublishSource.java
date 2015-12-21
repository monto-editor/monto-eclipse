package monto.eclipse.connection;

import org.json.simple.JSONObject;

import monto.eclipse.Activator;
import monto.service.version.VersionMessage;
import monto.service.version.VersionMessages;

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
