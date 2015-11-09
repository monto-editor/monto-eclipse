package monto.eclipse.connection;

import static monto.eclipse.OptionalUtils.withException;

import java.util.Optional;

import monto.eclipse.Activator;
import monto.service.discovery.Discoveries;
import monto.service.discovery.DiscoveryResponse;

public class SubscribeDiscover {
	private Subscribe connection;

	public SubscribeDiscover(Subscribe connection) {
		this.connection = connection;
	}
	
	public void connect() {
		connection.connect();
		connection.subscribe("discover");
	}
	
	public Optional<DiscoveryResponse> receiveMessage() {
		Optional<String> message = connection.receiveMessage();
		Optional<DiscoveryResponse> response = message.flatMap(withException(Discoveries::decode));
		Activator.debug("discovery response %s %s", message, response);
		return response;
	}
	
	public void cose() throws Exception {
		connection.close();
	}

	public void close() throws Exception {
		connection.close();
	}
}
