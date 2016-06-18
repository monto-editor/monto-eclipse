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
    this.address = address;
  }

  public void connect() {
    socket.connect(address);
  }

  public void subscribe(String sub) {
    socket.subscribe(sub.getBytes());
  }

  public Optional<String> receiveMessage() {
    String str = socket.recvStr();
    return Optional.ofNullable(str).map(ignore -> socket.recvStr());
  }

  public void setReceivedTimeout(int timeout) {
    socket.setReceiveTimeOut(timeout);
  }

  public void close() throws Exception {
    socket.close();
  }
}
