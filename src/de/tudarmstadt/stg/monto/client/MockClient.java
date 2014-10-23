package de.tudarmstadt.stg.monto.client;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.VersionMessage;

public class MockClient extends AbstractMontoClient {

	private List<Server> servers = new ArrayList<>();

	@Override
	public MontoClient sendVersionMessage(VersionMessage msg) {
		servers.forEach((Server server) ->
			server.apply(msg).ifPresent((productMsg) -> {
				registerProduct(msg.getSource(), productMsg.getProduct());
				listeners.forEach((listener) -> listener.onProductMessage(productMsg));
			})
		);
		return this;
	}

	public MockClient addServer(Server server) {
		servers.add(server);
		return this;
	}
	
	public MockClient removeServer(Server server) {
		servers.remove(server);
		return this;
	}

	@Override
	public void connect() throws Exception {
	}

	@Override
	public void listening() throws Exception {
	}
	
	@Override
	public void close() throws Exception {
	}

}
