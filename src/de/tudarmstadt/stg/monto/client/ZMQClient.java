package de.tudarmstadt.stg.monto.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class ZMQClient extends AbstractMontoClient implements AutoCloseable {

	private Connection connection;
	private Thread thread;
	private Map<Source,Set<Product>> registeredProducts;

	public ZMQClient() throws IOException, ConnectionParseException {
		this(Connection.create());
	}

	public ZMQClient(Connection connection) {
		this.connection = connection;
		this.registeredProducts = new HashMap<>();
	}
	
	@Override
	public MontoClient sendVersionMessage(VersionMessage msg) {
		connection.sendVersionMessage(msg);
		return this;
	}
	
	private void registerProduct(final Source source, final Product product) {
		registeredProducts.compute(source, (_source,products) -> {
			if(products == null)
				products = new HashSet<>();
			products.add(product);
			return products;
		});
	}

	@Override
	public Stream<Product> availableProducts(Source source, Language language) {
		return registeredProducts.get(source).stream();
	}

	@Override
	public void connect() throws Exception {
		connection.open();
	}
	
	@Override
	public void listening() throws Exception {
		thread = new Thread() {
			@Override
			public void run() {
				while(! this.isInterrupted()) {
					try {
						ProductMessage message = connection.receiveProductMessage();
						if(message != null) {
							registerProduct(message.getSource(),message.getProduct());
							listeners.forEach((listener) -> listener.onProductMessage(message));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	@Override
	public void close() throws Exception {
		thread.interrupt();
		thread.join();
		connection.close();
	}

}
