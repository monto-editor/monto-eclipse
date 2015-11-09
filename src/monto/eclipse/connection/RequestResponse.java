package monto.eclipse.connection;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import monto.eclipse.Time;

public class RequestResponse {
	private Context ctx;
	private String address;
	protected Socket socket;
	
	public RequestResponse(Context ctx, String address) {
		this.ctx = ctx;
		this.address = address;
	}
	
	public void connect() {
		socket = ctx.socket(ZMQ.REQ);
		socket.setLinger(Time.seconds(2));
		socket.connect(address);
	}
	
	public String request(String msg) {
		socket.send(msg);
		return new String(socket.recv());
	}
	
	public void close() {
		socket.close();
	}
}
