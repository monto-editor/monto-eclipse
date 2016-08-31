package monto.eclipse.launching;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamsProxy;

public class MontoStreamProxy implements IStreamsProxy {

  private MontoStreamMonitor stdoutMonitor;
  private MontoStreamMonitor stderrMonitor;

  public MontoStreamProxy() {
    stdoutMonitor = new MontoStreamMonitor();
    stderrMonitor = new MontoStreamMonitor();
  }

  @Override
  public MontoStreamMonitor getErrorStreamMonitor() {
    return stderrMonitor;
  }

  @Override
  public MontoStreamMonitor getOutputStreamMonitor() {
    return stdoutMonitor;
  }

  @Override
  public void write(String input) throws IOException {
    System.out.printf("MontoStreamProxy.write(%s)\n", input);
    // TODO: send CommandMessage
  }
}
