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
		socket = context.socket(ZMQ.REQ);
		socket.setReceiveTimeOut(Time.seconds(2));
		socket.setLinger(Time.seconds(2));
		socket.connect(connectionInfo);
	}

	public void sendMessage(String msg) throws Exception {
		socket.send(msg);
		byte[] ack = socket.recv();
		
		if(Connection.hasTimedOut(ack)) {
			
			// Close and reopen connection to be able to send messages the next time.
			socket.close();
			connect();
		}
	}

	public void close() throws Exception {
		socket.close();
	}
}
