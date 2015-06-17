package monto.eclipse.connection;

import monto.eclipse.Time;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Subscribe {
	private Context ctx;
	private String address;
	protected Socket socket;

	public Subscribe(Context ctx, String address) {
		this.ctx = ctx;
		this.address = address;
	}
	
	public void connect() {
		socket = ctx.socket(ZMQ.SUB);
		socket.setLinger(Time.seconds(2));
		socket.connect(address);
		socket.subscribe(new byte[] {});
	}
	
	public String receiveMessage() {
		return socket.recvStr();
	}
	
	public void close() throws Exception {
		socket.close();
	}
}
