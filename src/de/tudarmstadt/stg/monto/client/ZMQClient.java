package de.tudarmstadt.stg.monto.client;

import java.io.IOException;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class ZMQClient extends AbstractMontoClient {

	private Connection connection;
	private Thread thread;

	public ZMQClient() throws IOException, ConnectionParseException {
		this(Connection.create());
	}

	public ZMQClient(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public MontoClient sendVersionMessage(VersionMessage msg) {
		connection.sendVersionMessage(msg);
		return this;
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
