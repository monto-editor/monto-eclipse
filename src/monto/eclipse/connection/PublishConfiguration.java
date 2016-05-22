package monto.eclipse.connection;

import monto.eclipse.Activator;
import monto.service.configuration.Configuration;
import monto.service.gson.GsonMonto;

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
			connection.sendMessage(config.getServiceId().toString(), GsonMonto.toJson(config));
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	public void close() {
		connection.close();
	}
}
