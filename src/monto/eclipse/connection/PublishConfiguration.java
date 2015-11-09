package monto.eclipse.connection;

import org.json.simple.JSONObject;

import monto.eclipse.Activator;
import monto.service.configuration.Configurations;
import monto.service.configuration.ServiceConfiguration;

public class PublishConfiguration {
	private Publish connection;

	public PublishConfiguration(Publish connection) {
		this.connection = connection;
	}

	public void connect() {
		connection.connect();
	}
	
	public <T> void sendMessage(ServiceConfiguration message) {
		try {
			JSONObject encoding = Configurations.encode(message);
			connection.sendMessage(encoding.toJSONString());
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	public void close() {
		connection.close();
	}
}
