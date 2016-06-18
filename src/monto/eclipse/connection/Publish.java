package monto.eclipse.connection;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import monto.eclipse.Time;

public class Publish {

  private Context ctx;
  private String address;
  protected Socket socket;

  public Publish(Context ctx, String address) {
    this.ctx = ctx;
    this.address = address;
  }

  private void setupConnection() {
    socket = ctx.socket(ZMQ.PUB);
    socket.setLinger(Time.seconds(2));
  }

  public void connect() {
    setupConnection();
    socket.connect(address);
  }

  public void bind() {
    setupConnection();
    socket.bind(address);
  }

  public void sendMessage(String msg) throws Exception {
    sendMessage(null, msg);
  }

  public void sendMessage(String topic, String msg) throws Exception {
    if (topic != null)
      socket.sendMore(topic);
    socket.send(msg);
  }

  public void close() {
    socket.close();
  }
}
