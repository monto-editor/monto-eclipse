package de.tudarmstadt.stg.monto.connection;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Server;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.server.ProductMessageListener;

public class ServerConnection implements ProductMessageListener {
	
	private IncommingConnection incomming;
	private OutgoingConnection outgoing;
	private List<Server> servers = new ArrayList<>();

	public ServerConnection(IncommingConnection incomming, OutgoingConnection outgoing) {
		this.incomming = incomming;
		this.outgoing = outgoing;
	}

	public void addServer(Server server) {
		servers.add(server);
		server.addProductMessageListener(this);
	}
	
	public void removeServer(Server server) {
		servers.remove(server);
		server.removeProductMessageListener(this);
	}
	
	public void connect() throws Exception {
		incomming.connect();
		outgoing.connect();
	}
	
	public void listening() throws Exception {
		incomming.listening(reader -> {
			VersionMessage versionMessage;
			try {
				versionMessage = VersionMessage.decode(reader);
			} catch (Exception e) {
				Activator.error(e);
				return;
			}
			servers.forEach(listener ->
				listener.onVersionMessage(versionMessage)
			);
		});
	}
	
	@Override
	public void onProductMessage(ProductMessage message) {
		try {
			outgoing.sendMessage(ProductMessage.encode(message).toJSONString());
		} catch (Exception e) {
			Activator.debug("Could not send product message: %s,%s", message, e);
		}
	}
	
	public void close() throws Exception {
		incomming.close();
		outgoing.close();
	}
}
