package de.tudarmstadt.stg.monto.client;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class MockMontoClient extends AbstractMontoClient {

	private List<Server> servers = new ArrayList<>();

	@Override
	public MontoClient sendVersionMessage(VersionMessage msg) {
		servers.forEach((Server server) -> {
			final ProductMessage productMsg = server.apply(msg);

			listeners.forEach((listener) -> listener.onProductMessage(productMsg));
		});
		return this;
	}

	public MockMontoClient addServer(Server server) {
		servers.add(server);
		return this;
	}
	
	public MockMontoClient removeServer(Server server) {
		servers.remove(server);
		return this;
	}
}
