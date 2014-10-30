package de.tudarmstadt.stg.monto.connection;

import java.io.Reader;
import java.io.StringReader;
import java.util.function.Consumer;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import de.tudarmstadt.stg.monto.Time;

public class IncommingConnection {

	private Thread thread;
	
	private String connectionInfo;
	
	private Context context;
	private Socket socket = null;

	public IncommingConnection(Context context, String connectionInfo) {
		this.context = context;
		this.connectionInfo = connectionInfo;
	}
	
	public void connect() throws Exception {
		socket = context.socket(ZMQ.SUB);
		socket.connect(connectionInfo);
		socket.subscribe(new byte[]{});
		socket.setReceiveTimeOut(Time.seconds(2));
	}

	/**
	 * Either blocks under 2 seconds until a product message arrives
	 * or returns null if the connection has timed out.
	 */
	public String receiveMessage() {
		return socket.recvStr();
	}
	
	public void listening(Consumer<Reader> onMessage) throws Exception {
		thread = new Thread() {
			@Override
			public void run() {
				while(! this.isInterrupted()) {
					try {
						String message = receiveMessage();
						if(message != null) {
							onMessage.accept(new StringReader(message));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	public void close() throws Exception {
		thread.interrupt();
		thread.join();
		socket.close();
	}
}
