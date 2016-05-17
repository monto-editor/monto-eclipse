package monto.eclipse.connection;

import org.json.simple.JSONObject;

import monto.eclipse.Activator;
import monto.service.configuration.Configuration;
import monto.service.configuration.Configurations;

public class PublishConfiguration {
	private Publish connection;

	public PublishConfiguration(Publish connection) {
		this.connection = connection;
	}

	public void bind() {
		connection.bind();
	}
	
	public <T> void sendMessage(Configuration config) {
		try {
			JSONObject encoding = Configurations.encodeConfiguration(config);
			connection.sendMessage(config.getServiceId().toString(), encoding.toJSONString());
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	public void close() {
		connection.close();
	}
}
