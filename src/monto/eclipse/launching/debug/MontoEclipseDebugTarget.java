package monto.eclipse.launching.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import monto.eclipse.Activator;
import monto.eclipse.launching.MontoProcess;
import monto.service.gson.GsonMonto;
import monto.service.launching.debug.HitBreakpoint;
import monto.service.launching.debug.Thread;
import monto.service.product.ProductMessage;

public class MontoEclipseDebugTarget implements IDebugTarget {
  private final int sessionId;
  private final ILaunch launch;
  private final MontoProcess process;
  private List<MontoEclipseThread> threads;

  public MontoEclipseDebugTarget(int sessionId, ILaunch launch, MontoProcess process) {
    this.sessionId = sessionId;
    this.launch = launch;
    this.process = process;

    this.threads = new ArrayList<>();
  }

  @Override
  public String getModelIdentifier() {
    return Activator.PLUGIN_ID;
  }

  @Override
  public IDebugTarget getDebugTarget() {
    return this;
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    System.out.printf("MontoDebugTarget.getAdapter(%s)\n", adapter);
    return null;
  }

  @Override
  public boolean canTerminate() {
    return true;
  }

  @Override
  public boolean isTerminated() {
    return process.isTerminated();
  }

  @Override
  public void terminate() throws DebugException {
    System.out.println("MontoDebugTarget.terminate()");
    // Activator.sendCommandMessage(TerminateProcess.createCommandMessage(sessionId, /* TODO */ 1,
    // new ServiceId("javaDebugger")));
    // TODO
  }

  @Override
  public boolean canResume() {
    // TODO
    return false;
  }

  @Override
  public boolean canSuspend() {
    // TODO
    return false;
  }

  @Override
  public boolean isSuspended() {
    // TODO
    return false;
  }

  @Override
  public void resume() throws DebugException {
    // TODO
    System.out.println("MontoDebugTarget.resume()");
  }

  @Override
  public void suspend() throws DebugException {
    // TODO
    System.out.println("MontoDebugTarget.suspend()");
  }

  @Override
  public void breakpointAdded(IBreakpoint breakpoint) {
    System.out.printf("MontoDebugTarget.breakpointAdded(%s)\n", breakpoint);
  }

  @Override
  public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
    System.out.printf("MontoDebugTarget.breakpointRemoved(%s, %s)\n", breakpoint, delta);
  }

  @Override
  public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
    System.out.printf("MontoDebugTarget.breakpointChanged(%s, %s)\n", breakpoint, delta);
    // TODO
  }

  @Override
  public boolean canDisconnect() {
    // TODO
    return false;
  }

  @Override
  public void disconnect() throws DebugException {
    // TODO
    System.out.println("MontoDebugTarget.disconnect()");
  }

  @Override
  public boolean isDisconnected() {
    return false;
  }

  @Override
  public boolean supportsStorageRetrieval() {
    return false;
  }

  @Override
  public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoDebugTarget does not support memory block retrieval"));
  }

  @Override
  public IProcess getProcess() {
    return process;
  }

  @Override
  public IThread[] getThreads() throws DebugException {
    return threads.stream().toArray(MontoEclipseThread[]::new);
  }

  @Override
  public boolean hasThreads() throws DebugException {
    return false;
  }

  @Override
  public String getName() throws DebugException {
    return "Monto Debug Target";
  }

  @Override
  public boolean supportsBreakpoint(IBreakpoint breakpoint) {
    return true;
  }
  
  public void fireEvent(int eventKindId, int eventDetailId) {
    DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(this, eventKindId, eventDetailId)});
  }

  void onBreakpointHit(ProductMessage productMessage) {
    HitBreakpoint hitBreakpoint = GsonMonto.fromJson(productMessage, HitBreakpoint.class);
    
    MontoEclipseThread hitThread = convertMontoToEclipseThread(this, hitBreakpoint.getHitThread());
    threads.clear();
    threads.add(hitThread);
    for (Thread montoThread : hitBreakpoint.getOtherThreads()) {
      threads.add(convertMontoToEclipseThread(this, montoThread));
    }
    fireEvent(DebugEvent.SUSPEND, DebugEvent.BREAKPOINT);
    //TODO: maybe convert jsonelement directly to Monto Eclipse classes
  }

  private MontoEclipseThread convertMontoToEclipseThread(MontoEclipseDebugTarget debugTarget,
      Thread montoThread) {
    MontoEclipseThread thread = new MontoEclipseThread(debugTarget, montoThread.getName());

    MontoEclipseStackFrame[] stackFrames =
        montoThread.getStackFrames().stream().map(montoStackFrame -> {
          MontoEclipseStackFrame stackFrame =
              new MontoEclipseStackFrame(debugTarget, montoStackFrame.getName());

          MontoEclipseVariable[] variables = montoStackFrame.getVariables().stream().map(montoVariable -> {
            MontoEclipseVariable variable = new MontoEclipseVariable(debugTarget,
                montoVariable.getName(), montoVariable.getType());
            MontoEclipseValue value = new MontoEclipseValue(debugTarget, montoVariable.getValue());

            variable._setValue(value);
            value._setVariables(new MontoEclipseVariable[] {variable});
            return variable;
          }).toArray(MontoEclipseVariable[]::new);
          
          stackFrame._setThread(thread);
          stackFrame._setVariables(variables);
          return stackFrame;
        }).toArray(MontoEclipseStackFrame[]::new);

    thread._setStackFrames(stackFrames);
    return thread;
  }

}
