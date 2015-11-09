package monto.eclipse.connection;

import java.util.Optional;

import monto.eclipse.Activator;
import monto.service.message.ProductMessage;
import monto.service.message.ProductMessages;

public class Sink {
	private Subscribe connection;
	private String serviceId;

	public Sink(Subscribe connection, String serviceId) {
		this.connection = connection;
		this.serviceId = serviceId;
	}
	
	public void connect() {
		connection.connect();
		connection.subscribe(serviceId);
	}
	
	public Optional<ProductMessage> receiveMessage() {
		return connection.receiveMessage()
                .flatMap(msg -> {
                	try {
                		return Optional.of(ProductMessages.decode(msg));
                	} catch(Exception e) {
                		Activator.error("Could not decode product message: "+msg, e);
                		return Optional.empty();
                	}
                });
	}
	
	public void close() throws Exception {
		connection.close();
	}
}
