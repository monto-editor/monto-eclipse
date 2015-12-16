package monto.eclipse.connection;

import org.json.simple.JSONObject;

import monto.eclipse.Activator;
import monto.service.message.ConfigurationMessage;
import monto.service.message.ConfigurationMessages;

public class PublishConfiguration {
	private Publish connection;

	public PublishConfiguration(Publish connection) {
		this.connection = connection;
	}

	public void bind() {
		connection.bind();
	}
	
	public <T> void sendMessage(ConfigurationMessage message) {
		try {
			JSONObject encoding = ConfigurationMessages.encode(message);
			connection.sendMessage(message.getServiceID().toString(), encoding.toJSONString());
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	public void close() {
		connection.close();
	}
}
