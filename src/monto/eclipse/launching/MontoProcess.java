package monto.eclipse.launching;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import monto.service.gson.GsonMonto;
import monto.service.product.ProductMessage;
import monto.service.run.ProcessTerminated;
import monto.service.run.StreamOutput;

public class MontoProcess implements IProcess {

  private ILaunch launch;
  private MontoStreamProxy streamProxy;
  private boolean terminated;
  private int exitCode;

  public MontoProcess(ILaunch launch) {
    this.launch = launch;
    this.streamProxy = new MontoStreamProxy();
    this.terminated = false;
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    System.out.printf("MontoProcess.getAdapter(%s)\n", adapter);
    return null;
  }

  @Override
  public boolean canTerminate() {
    // TODO: implement termination
    return false;
  }

  @Override
  public boolean isTerminated() {
    return terminated;
  }

  @Override
  public void terminate() throws DebugException {
    // TODO: send terminate CommandMessage
  }

  @Override
  public String getLabel() {
    return "session" + "X";
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }

  @Override
  public IStreamsProxy getStreamsProxy() {
    return streamProxy;
  }

  @Override
  public void setAttribute(String key, String value) {
    // not needed yet
    System.out.printf("MontoProcess.setAttribute(%s, %s)\n", key, value);
  }

  @Override
  public String getAttribute(String key) {
    // not needed yet
    System.out.printf("MontoProcess.getAttribute(%s)\n", key);
    return null;
  }

  @Override
  public int getExitValue() throws DebugException {
    return exitCode;
  }

  void onTerminationProduct(ProductMessage productMessage) {
    ProcessTerminated processTerminated =
        GsonMonto.fromJson(productMessage, ProcessTerminated.class);
    terminated = true;
    exitCode = processTerminated.getExitCode();
  }

  void onStreamOutputProduct(ProductMessage productMessage) {
    StreamOutput streamOutput = GsonMonto.fromJson(productMessage, StreamOutput.class);
    System.out.println(streamOutput);
    switch (streamOutput.getSourceStream()) {
      case OUT:
        streamProxy.getOutputStreamMonitor().fireEvent(streamOutput.getData());
        break;
      case ERR:
        streamProxy.getErrorStreamMonitor().fireEvent(streamOutput.getData());
        break;
    }
  }
}
