package monto.eclipse.connection;

import static monto.eclipse.OptionalUtils.withException;

import java.util.Optional;

import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;
import monto.service.gson.GsonMonto;

public class Discovery {
	private RequestResponse connection;

	public Discovery(RequestResponse connection) {
		this.connection = connection;
	}

	public Optional<DiscoveryResponse> discoveryRequest(DiscoveryRequest request) {
		Optional<String> response = Optional.ofNullable(connection.request(GsonMonto.toJson(request)));
		return response.flatMap(withException(str -> GsonMonto.fromJson(str, DiscoveryResponse.class)));
	}

	public void connect() {
		connection.connect();
	}

	public void close() {
		connection.close();
	}
}
