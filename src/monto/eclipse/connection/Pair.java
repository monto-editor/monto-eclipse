package monto.eclipse.connection;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import monto.eclipse.Time;

public class Pair {
	
	private Context ctx;
	private String address;
	protected Socket socket;

	public Pair(Context ctx, String address) {
		this.ctx = ctx;
		this.address = address;
	}
	
	public void connect() {
		socket = ctx.socket(ZMQ.PAIR);
		socket.setLinger(Time.seconds(2));
		socket.connect(address);
	}
	
	public void sendMessage(String msg) throws Exception {
		socket.send(msg);
	}
	
	public String receiveMessage() {
		return socket.recvStr();
	}
	
	public void close() throws Exception {
		socket.close();
	}
}
