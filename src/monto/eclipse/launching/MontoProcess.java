package monto.eclipse.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import monto.eclipse.Activator;
import monto.service.gson.GsonMonto;
import monto.service.product.ProductMessage;
import monto.service.launching.ProcessTerminated;
import monto.service.launching.StreamOutput;
import monto.service.launching.StreamOutput.SourceStream;

public class MontoProcess implements IProcess {

  private ILaunch launch;
  private int sessionId;
  private MontoStreamProxy streamProxy;
  private boolean terminated;
  private int exitCode;

  public MontoProcess(ILaunch launch, int sessionId) {
    this.launch = launch;
    this.sessionId = sessionId;

    this.streamProxy = new MontoStreamProxy();
    this.terminated = false;
    this.exitCode = -1999;
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
    return "session" + sessionId;
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
    if (!terminated) {
      throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          "Can't get exit value of running Monto process"));
    }
    return exitCode;
  }

  void onProcessTerminatedProduct(ProductMessage productMessage) {
    ProcessTerminated processTerminated =
        GsonMonto.fromJson(productMessage, ProcessTerminated.class);
    if (sessionId == processTerminated.getSession()) {
      terminated = true;
      exitCode = processTerminated.getExitCode();
      System.out.println("Process " + sessionId + " terminated");
      if (launch instanceof Launch) {
        Launch castedLaunch = (Launch) launch;
        castedLaunch
            .handleDebugEvents(new DebugEvent[] {new DebugEvent(this, DebugEvent.TERMINATE)});
      }
    }
  }

  void onStreamOutputProduct(ProductMessage productMessage) {
    StreamOutput streamOutput = GsonMonto.fromJson(productMessage, StreamOutput.class);
    if (sessionId == streamOutput.getSession()) {
      System.out.println(streamOutput);
      SourceStream sourceStream = streamOutput.getSourceStream();
      if (sourceStream == SourceStream.OUT) {
        streamProxy.getOutputStreamMonitor().fireEvent(streamOutput.getData());
      } else if (sourceStream == SourceStream.ERR) {
        streamProxy.getErrorStreamMonitor().fireEvent(streamOutput.getData());
      }
    }
  }
}
