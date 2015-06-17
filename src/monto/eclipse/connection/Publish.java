package monto.eclipse.connection;

import monto.eclipse.Time;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Publish {

	private Context ctx;
	private String address;
	protected Socket socket;

	public Publish(Context ctx, String address) {
		this.ctx = ctx;
		this.address = address;
	}
	
	public void connect() {
		socket = ctx.socket(ZMQ.PUB);
		socket.setLinger(Time.seconds(2));
		socket.connect(address);
	}
	
	public void sendMessage(String msg) throws Exception {
		socket.send(msg);
	}

	public void close() {
		socket.close();
	}
}
