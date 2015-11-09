package monto.eclipse.connection;

import static monto.eclipse.OptionalUtils.withException;

import java.util.Optional;

import org.json.simple.JSONObject;

import monto.service.discovery.Discoveries;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;

public class Discovery {
	private RequestResponse connection;

	public Discovery(RequestResponse connection) {
		this.connection = connection;
	}
	
	public Optional<DiscoveryResponse> discoveryRequest(DiscoveryRequest request) {
		JSONObject encoding = Discoveries.encode(request);
		return Optional.ofNullable(connection.request(encoding.toJSONString()))
						.flatMap(withException(Discoveries::decode));
	}

	public void connect() {
		connection.connect();
	}

	public void close() {
		connection.close();
	}
}
