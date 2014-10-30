package de.tudarmstadt.stg.monto.connection;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.server.Server;

public class ServerConnection {
	
	private IncommingConnection incomming;
	private OutgoingConnection outgoing;
	private List<Server> servers = new ArrayList<>();

	public ServerConnection(IncommingConnection incomming, OutgoingConnection outgoing) {
		this.incomming = incomming;
		this.outgoing = outgoing;
	}

	public void addServer(Server server) {
		servers.add(server);
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
			servers.forEach(server ->
				server.apply(versionMessage)
					  .ifPresent(productMessage ->
					  	send(ProductMessage.encode(productMessage).toJSONString())
				)
			);
		});
	}
	
	private void send(String message) {
		try {
			outgoing.sendMessage(message);
		} catch (Exception e) {
			Activator.error(e);
		}
	}
	
	public void close() throws Exception {
		incomming.close();
		outgoing.close();
	}

}
