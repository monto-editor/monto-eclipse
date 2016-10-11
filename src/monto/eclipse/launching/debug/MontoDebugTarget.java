package monto.eclipse.launching.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import monto.eclipse.Activator;
import monto.eclipse.launching.MontoProcess;
import monto.service.gson.GsonMonto;
import monto.service.launching.debug.Breakpoint;
import monto.service.launching.debug.HitBreakpoint;
import monto.service.launching.debug.Thread;
import monto.service.product.ProductMessage;

public class MontoDebugTarget extends MontoDebugElement implements IDebugTarget {
  private final int sessionId;
  private final ILaunch launch;
  private final MontoProcess process;
  private List<MontoThread> threads;
  private boolean isSuspended;

  public MontoDebugTarget(int sessionId, ILaunch launch, MontoProcess process) {
    super(null);
    super.debugTarget = this;
    this.sessionId = sessionId;
    this.launch = launch;
    this.process = process;
    this.threads = new ArrayList<>();
    this.isSuspended = false;
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }

  @Override
  public boolean canTerminate() {
    System.out.println("MontoDebugTarget.canTerminate()");
    return process.canTerminate();
  }

  @Override
  public boolean isTerminated() {
    System.out.println("MontoDebugTarget.isTerminated()");
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
    System.out.println("MontoDebugTarget.canResume()");
    // TODO
    return false;
  }

  @Override
  public boolean canSuspend() {
    System.out.println("MontoDebugTarget.canSuspend()");
    // TODO
    return false;
  }

  @Override
  public boolean isSuspended() {
    System.out.println("MontoDebugTarget.isSuspended()");
    // TODO
    return isSuspended;
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
    System.out.println("MontoDebugTarget.canDisconnect()");
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
    System.out.println("MontoDebugTarget.isDisconnected()");
    return false;
  }

  @Override
  public boolean supportsStorageRetrieval() {
    System.out.println("MontoDebugTarget.supportsStorageRetrieval()");
    return false;
  }

  @Override
  public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoDebugTarget does not support memory block retrieval"));
  }

  @Override
  public IProcess getProcess() {
    System.out.println("MontoDebugTarget.getProcess()");
    return process;
  }

  @Override
  public IThread[] getThreads() throws DebugException {
    System.out.println("MontoDebugTarget.getThreads()");
    return threads.stream().toArray(MontoThread[]::new);
  }

  @Override
  public boolean hasThreads() throws DebugException {
    System.out.println("MontoDebugTarget.hasThreads()");
    return !threads.isEmpty();
  }

  @Override
  public String getName() throws DebugException {
    return "Monto Debug Target";
  }

  @Override
  public boolean supportsBreakpoint(IBreakpoint breakpoint) {
    System.out.println("MontoDebugTarget.supportsBreakpoint()");
    return true;
  }

  public void onBreakpointHit(ProductMessage productMessage) {
    System.out.println("MontoDebugTarget.onBreakpointHit()");
    HitBreakpoint hitBreakpoint = GsonMonto.fromJson(productMessage, HitBreakpoint.class);

    if (productMessage.matchesFakeSource("debug", sessionId)) {
      isSuspended = true;

      MontoThread hitThread = convertMontoToEclipseThread(this, hitBreakpoint.getHitThread());
      threads.clear();
      threads.add(hitThread);
      for (Thread montoThread : hitBreakpoint.getOtherThreads()) {
        threads.add(convertMontoToEclipseThread(this, montoThread));
      }
      threads.forEach(thread -> thread.fireEvent(DebugEvent.SUSPEND, DebugEvent.BREAKPOINT));
    }
  }

  private MontoThread convertMontoToEclipseThread(MontoDebugTarget debugTarget,
      Thread montoThread) {
    MontoThread thread = new MontoThread(debugTarget, montoThread.getName());
    
    MontoLineBreakpoint suspendingBreakpoint =
        findEclipseLineBreakpoint(montoThread.getSuspendingBreakpoint());
    MontoLineBreakpoint[] suspendingBreakpoints;
    if (suspendingBreakpoint == null) {
      suspendingBreakpoints = new MontoLineBreakpoint[] {};
    } else {
      suspendingBreakpoints = new MontoLineBreakpoint[] {suspendingBreakpoint};
    }
    thread._setSuspendingBreakpoints(suspendingBreakpoints);

    MontoStackFrame[] stackFrames = montoThread.getStackFrames().stream().map(montoStackFrame -> {
      MontoStackFrame stackFrame = new MontoStackFrame(debugTarget, montoStackFrame.getName());

      MontoVariable[] variables = montoStackFrame.getVariables().stream().map(montoVariable -> {
        MontoVariable variable =
            new MontoVariable(debugTarget, montoVariable.getName(), montoVariable.getType());
        MontoValue value = new MontoValue(debugTarget, montoVariable.getValue());

        variable._setValue(value);
        // variables in value represent inner fields
        value._setVariables(new MontoVariable[] {});
        return variable;
      }).toArray(MontoVariable[]::new);

      stackFrame._setThread(thread);
      stackFrame._setVariables(variables);
      return stackFrame;
    }).toArray(MontoStackFrame[]::new);

    thread._setStackFrames(stackFrames);
    return thread;
  }

  private MontoLineBreakpoint findEclipseLineBreakpoint(Breakpoint breakpoint) {
    if (breakpoint != null) {
      IBreakpoint[] eclipseBreakpoints =
          DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(Activator.PLUGIN_ID);

      for (IBreakpoint eclipseBreakpoint : eclipseBreakpoints) {
        if (eclipseBreakpoint instanceof MontoLineBreakpoint) {
          MontoLineBreakpoint eclipseMontoBreakpoint = (MontoLineBreakpoint) eclipseBreakpoint;
          try {
            if (eclipseMontoBreakpoint.getSource().equals(breakpoint.getSource())
                && eclipseMontoBreakpoint.getLineNumber() == breakpoint.getLineNumber()) {
              return eclipseMontoBreakpoint;
            }
          } catch (DebugException ignored) {
          }
        }
      }
    }
    return null;
  }
}
