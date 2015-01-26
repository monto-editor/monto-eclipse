package de.tudarmstadt.stg.monto.connection;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessages;
import de.tudarmstadt.stg.monto.message.VersionMessages;
import de.tudarmstadt.stg.monto.server.Server;

public abstract class AbstractServer implements Server {
	
	private Pair connection;
	private boolean running = true;
	private Thread thread = null;
	
	public AbstractServer(Pair connection) {
		this.connection = connection;
	}
	
	public void sendMessage(ProductMessage message) {
		try {
			JSONObject encoding = ProductMessages.encode(message);
			connection.sendMessage(encoding.toJSONString());
		} catch (Exception e) {
			Activator.error(e);
		}
	}
	
	@Override
	public void fork() {
		AbstractServer that = this;
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
	
	public void run() throws Exception {
		running = true;
		while(running) {
			JSONArray messages = (JSONArray) JSONValue.parse(connection.receiveMessage());
			List<Message> decodedMessages = new ArrayList<>();
			for(Object messageObj : messages) {
				JSONObject message = (JSONObject) messageObj;
				Message decoded = message.containsKey("product") ? ProductMessages.decode(message) : VersionMessages.decode(message);
				decodedMessages.add(decoded);
			}
			this.onMessage(decodedMessages).match(
					ex -> { Activator.error(ex); return null; },
					msg -> { sendMessage(msg); return null; });
		}
		connection.close();
	}
	
	@Override
	public void stop() {
		if(running = true) {
			running = false;
			thread.interrupt();
		}
	}
}
