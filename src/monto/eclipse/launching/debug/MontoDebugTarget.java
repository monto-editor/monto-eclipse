package monto.eclipse.launching.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
import monto.eclipse.demultiplex.SinkDemultiplexer;
import monto.eclipse.launching.MontoProcess;
import monto.service.command.CommandMessage;
import monto.service.command.Commands;
import monto.service.gson.GsonMonto;
import monto.service.launching.debug.Breakpoint;
import monto.service.launching.debug.HitBreakpoint;
import monto.service.launching.debug.Thread;
import monto.service.product.ProductMessage;
import monto.service.product.Products;
import monto.service.types.Language;
import monto.service.types.Source;

public class MontoDebugTarget extends MontoDebugElement implements IDebugTarget {
  private final int sessionId;
  private final Source sessionSource;
  private final Language language;
  private final ILaunch launch;
  private final MontoProcess process;
  private final List<MontoThread> threads;

  private boolean isSuspended;

  public MontoDebugTarget(int sessionId, Language language, ILaunch launch, MontoProcess process) {
    super(null);
    super.debugTarget = this;
    this.sessionId = sessionId;
    this.sessionSource = new Source("session:" + sessionId);
    this.language = language;
    this.launch = launch;
    this.process = process;
    this.threads = new ArrayList<>();

    this.isSuspended = false;
  }

  /* MONTO MODEL METHODS */

  public int getSessionId() {
    return sessionId;
  }

  public Source getSessionSource() {
    return sessionSource;
  }

  public Language getLanguage() {
    return language;
  }



  /* ECLIPSE MODEL METHODS */

  @Override
  public IProcess getProcess() {
    return process;
  }

  @Override
  public IThread[] getThreads() throws DebugException {
    return threads.stream().toArray(MontoThread[]::new);
  }

  @Override
  public boolean hasThreads() throws DebugException {
    return !threads.isEmpty();
  }

  @Override
  public String getName() throws DebugException {
    return "Monto Debug Target";
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }



  /* PROCESS DELEGATE METHODS */

  @Override
  public boolean canTerminate() {
    return process.canTerminate();
  }

  @Override
  public boolean isTerminated() {
    return process.isTerminated();
  }

  @Override
  public void terminate() throws DebugException {
    process.terminate();
  }



  /* CAPABILITIES AND COMMANDS */

  @Override
  public boolean canResume() {
    return !isTerminated() && isSuspended;
  }

  @Override
  public boolean canSuspend() {
    // TODO
    return false;
  }

  @Override
  public boolean isSuspended() {
    return isSuspended;
  }

  @Override
  public void resume() throws DebugException {
    Activator.sendCommandMessage(new CommandMessage(sessionId, 0, Commands.DEBUG_RESUME, language,
        GsonMonto.toJsonTree(null)));
  }

  @Override
  public void suspend() throws DebugException {
    // TODO
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoDebugTarget doesn't support suspension"));
  }

  @Override
  public boolean canDisconnect() {
    return false;
  }

  @Override
  public void disconnect() throws DebugException {
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoDebugTarget doesn't support disconnecting"));
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
  public boolean supportsBreakpoint(IBreakpoint breakpoint) {
    return true;
  }



  /* BREAKPOINT EVENT HANDLING */

  @Override
  public void breakpointAdded(IBreakpoint breakpoint) {
    System.out.printf("MontoDebugTarget.breakpointAdded(%s)\n", breakpoint);

    Optional<Breakpoint> maybeBreakpoint = convertIBreakpointToBreakpoint(breakpoint);
    if (maybeBreakpoint.isPresent()) {
      Activator.sendCommandMessage(new CommandMessage(sessionId, 0, Commands.ADD_BREAKPOINT,
          language, GsonMonto.toJsonTree(maybeBreakpoint.get())));
    }
  }

  @Override
  public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
    System.out.printf("MontoDebugTarget.breakpointRemoved(%s, %s)\n", breakpoint, delta);

    Optional<Breakpoint> maybeBreakpoint = convertIBreakpointToBreakpoint(breakpoint);
    if (maybeBreakpoint.isPresent()) {
      Activator.sendCommandMessage(new CommandMessage(sessionId, 0, Commands.REMOVE_BREAKPOINT,
          language, GsonMonto.toJsonTree(maybeBreakpoint.get())));
    }
  }

  private Optional<Breakpoint> convertIBreakpointToBreakpoint(IBreakpoint breakpoint) {
    return Stream.of(breakpoint)
        .filter(eclipseBreakpoint -> (eclipseBreakpoint != null
            && eclipseBreakpoint instanceof MontoLineBreakpoint))
        .map(MontoLineBreakpoint.class::cast).flatMap(MontoLineBreakpoint::getBreakpointStream)
        .findFirst();
  }

  @Override
  public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}

  public void onBreakpointHit(ProductMessage productMessage) {
    System.out.println("MontoDebugTarget.onBreakpointHit()");
    HitBreakpoint hitBreakpoint = GsonMonto.fromJson(productMessage, HitBreakpoint.class);

    if (productMessage.getSource().equals(sessionSource)) {
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

  public void onThreadsResumed(ProductMessage productMessage) {
    System.out.println("MontoDebugTarget.onThreadsResumed()");

    if (productMessage.getSource().equals(sessionSource)) {
      isSuspended = false;
      threads.clear();
      fireEvent(DebugEvent.RESUME);
    }
  }

  private MontoThread convertMontoToEclipseThread(MontoDebugTarget debugTarget,
      Thread montoThread) {
    MontoThread thread = new MontoThread(debugTarget, montoThread);

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

  public void onThreadStepped(ProductMessage productMessage) {
    if (productMessage.getSource().equals(sessionSource)) {
      Thread thread = GsonMonto.fromJson(productMessage, Thread.class);
      MontoThread montoThread = convertMontoToEclipseThread(debugTarget, thread);

      // indexOf() works, because MontoThread implements equals() which gets forwarded to Thread
      threads.set(threads.indexOf(montoThread), montoThread);

      // children of this MontoDebugTarget changed
      this.fireEvent(DebugEvent.CHANGE, DebugEvent.CONTENT);

      // new MontoThread has finished step
      montoThread.fireEvent(DebugEvent.SUSPEND, DebugEvent.STEP_END);
    }
  }

  public void onProcessTerminated(ProductMessage productMessage) {
    if (productMessage.getSource().equals(sessionSource)) {
      DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);

      // deregister product listeners

      // Deregistration needs to happen is separate thread, because removing listeners in this
      // callback method, which is called from the SinkDemultiplexer thread, causes a
      // ConcurrentModification exception in the SinkDemultiplexer thread, because the listener
      // map/list is SinkDemultiplexer is modified, before all listeners are called
      CompletableFuture.runAsync(() -> {
        SinkDemultiplexer demultiplexer = Activator.getDefault().getDemultiplexer();
        demultiplexer.removeProductListener(Products.HIT_BREAKPOINT, this);
        demultiplexer.removeProductListener(Products.THREADS_RESUMED, this);
        demultiplexer.removeProductListener(Products.PROCESS_TERMINATED, this);
      });
    }
  }
}
