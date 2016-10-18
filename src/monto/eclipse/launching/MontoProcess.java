package monto.eclipse.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import monto.eclipse.Activator;
import monto.service.command.CommandMessage;
import monto.service.command.Commands;
import monto.service.gson.GsonMonto;
import monto.service.launching.ProcessTerminated;
import monto.service.launching.StreamOutput;
import monto.service.launching.StreamOutput.SourceStream;
import monto.service.product.ProductMessage;
import monto.service.product.Products;
import monto.service.types.Language;
import monto.service.types.Source;

public class MontoProcess extends PlatformObject implements IProcess {

  private final ILaunch launch;
  private final int sessionId;
  private final Source sessionSource;
  private final String mode;
  private final Language language;

  private final MontoStreamProxy streamProxy;
  private boolean terminated;
  private int exitCode;


  public MontoProcess(ILaunch launch, int sessionId, String mode, Language language) {
    this.launch = launch;
    this.sessionId = sessionId;
    this.sessionSource = new Source(String.format("session:%s", sessionId));
    this.mode = mode;
    this.language = language;

    this.streamProxy = new MontoStreamProxy();
    this.terminated = false;
    this.exitCode = -1999;
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    return super.getAdapter(adapter);
  }

  @Override
  public boolean canTerminate() {
    return !terminated;
  }

  @Override
  public boolean isTerminated() {
    return terminated;
  }

  @Override
  public void terminate() throws DebugException {
    System.out.println("MontoProcess.terminate()");
    Activator.sendCommandMessage(
        new CommandMessage(sessionId, 0, Commands.TERMINATE_PROCESS, language, null));
  }

  @Override
  public String getLabel() {
    return String.format("Monto %s session %d", mode, sessionId);
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
    if (productMessage.getSource().equals(sessionSource)) {
      ProcessTerminated processTerminated =
          GsonMonto.fromJson(productMessage, ProcessTerminated.class);

      terminated = true;
      exitCode = processTerminated.getExitCode();
      System.out.println("Process " + sessionId + " terminated");
      DebugPlugin.getDefault()
          .fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, DebugEvent.TERMINATE)});

      // deregister product listeners
      Activator.getDefault().getDemultiplexer().removeProductListener(Products.PROCESS_TERMINATED,
          this::onProcessTerminatedProduct);
      Activator.getDefault().getDemultiplexer().removeProductListener(Products.STREAM_OUTPUT,
          this::onStreamOutputProduct);
    }
  }

  void onStreamOutputProduct(ProductMessage productMessage) {
    if (productMessage.getSource().equals(sessionSource)) {
      StreamOutput streamOutput = GsonMonto.fromJson(productMessage, StreamOutput.class);

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
