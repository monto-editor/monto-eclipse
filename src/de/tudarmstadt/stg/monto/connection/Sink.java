package de.tudarmstadt.stg.monto.connection;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessages;

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
