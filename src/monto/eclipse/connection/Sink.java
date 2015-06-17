package monto.eclipse.connection;

import monto.eclipse.Activator;
import monto.eclipse.message.ProductMessage;
import monto.eclipse.message.ProductMessages;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public abstract class Sink {

	private Subscribe connection;
	private boolean running = true;
	private Thread thread = null;
	
	public Sink(Subscribe connection) {
		this.connection = connection;
	}
	
	public void fork() {
		Sink that = this;
		connection.connect();
		thread  = new Thread() {
			@Override
			public void run() {
				try {
					that.run();
				} catch (Exception e) {
					Activator.error(e);
				}
			}
		};
		thread.start();
	}

	protected void run() throws Exception {
		running = true;
		while(running) {
			ProductMessage decoded = ProductMessages.decode((JSONObject) JSONValue.parse(connection.receiveMessage()));
			this.onMessage(decoded);
		}
		connection.close();
	}
	
	public abstract void onMessage(ProductMessage decodedMessages);

	public void stop() {
		if(running = true) {
			running = false;
			thread.interrupt();
		}
	}
}
