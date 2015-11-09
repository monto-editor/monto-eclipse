package monto.eclipse.connection;

import java.util.Optional;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Subscribe {
	protected Socket socket;
	private String address;

	public Subscribe(Context ctx, String address) {
		socket = ctx.socket(ZMQ.SUB);
		socket.setReceiveTimeOut(500);
		this.address = address;
    }
	
	public void connect() {
		socket.connect(address);
	}
	
	public void subscribe(String sub) {
		socket.subscribe(sub.getBytes());
	}
	
	public Optional<String> receiveMessage() {
		return Optional.ofNullable(socket.recvStr())
				.map(ignore -> socket.recvStr());
	}
	
	public void close() throws Exception {
		socket.close();
	}
}
