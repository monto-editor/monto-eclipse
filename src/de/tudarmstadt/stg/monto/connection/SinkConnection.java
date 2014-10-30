package de.tudarmstadt.stg.monto.connection;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessageParseException;
import de.tudarmstadt.stg.monto.message.ProductRegistry;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.sink.Sink;

public class SinkConnection {
	
	private IncommingConnection connection;
	private ProductRegistry availableProducts = new ProductRegistry();
	private List<Sink> sinks = new ArrayList<>();
	
	public SinkConnection(IncommingConnection connection) {
		this.connection = connection;
	}

	public void addSink(Sink sink) {
		sinks.add(sink);
	}
	
	public void removeSink(Sink sink) {
		sinks.remove(sink);
	}
	
	public void connect() throws Exception {
		connection.connect();
	}
	
	public ProductMessage receiveProductMessage() throws ProductMessageParseException {
		return ProductMessage.decode(new StringReader(connection.receiveMessage()));
	}
	
	public void listening() throws Exception {
		connection.listening(reader -> {
			try {
				ProductMessage message = ProductMessage.decode(reader);
				availableProducts.registerProduct(message.getSource(), message.getProduct());
				sinks.forEach(sink -> sink.onProductMessage(message));
			} catch (Exception e) {
				Activator.error(e);
			}
		});
	}
	
	public Set<Product> availableProducts(Source source) {
		return availableProducts.availableProducts(source);
	}
	
	public void close() throws Exception {
		connection.close();
	}

}
