package monto.eclipse;

import java.util.Optional;

import monto.service.product.ProductMessage;
import monto.service.product.ProductMessages;

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
                		return Optional.of((ProductMessage) ProductMessages.decode(msg));
                	} catch(Exception e) {
                		return Optional.empty();
                	}
                });
	}
	
	public void close() throws Exception {
		connection.close();
	}
}