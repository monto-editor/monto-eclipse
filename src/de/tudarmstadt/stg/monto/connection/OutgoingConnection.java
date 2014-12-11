package de.tudarmstadt.stg.monto.connection;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import de.tudarmstadt.stg.monto.Time;

public class OutgoingConnection {
	
	private Context context;
	private Socket socket = null;
	private String connectionInfo;

	public OutgoingConnection(Context context, String connectionInfo) {
		this.context = context;
		this.connectionInfo = connectionInfo;
	}

	public void connect() throws Exception {
		socket = context.socket(ZMQ.PUB);
		socket.setLinger(Time.seconds(2));
		socket.connect(connectionInfo);
	}

	public synchronized void sendMessage(String msg) throws Exception {
		socket.send(msg);
	}

	public void close() throws Exception {
		socket.close();
	}
}
