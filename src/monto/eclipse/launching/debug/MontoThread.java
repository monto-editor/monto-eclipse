package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class MontoThread extends MontoDebugElement implements IThread {

  private final String name;
  private MontoStackFrame[] stackFrames;
  private MontoLineBreakpoint[] suspendingBreakpoints;

  public MontoThread(MontoDebugTarget debugTarget, String name) {
    super(debugTarget);
    this.name = name;
  }

  void _setStackFrames(MontoStackFrame[] stackFrames) {
    this.stackFrames = stackFrames;
  }
  
  void _setSuspendingBreakpoints(MontoLineBreakpoint[] suspendingBreakpoints) {
    this.suspendingBreakpoints = suspendingBreakpoints;
  }

  @Override
  public boolean canResume() {
    System.out.println("MontoThread.canResume()");
    return isSuspended();
  }

  @Override
  public boolean canSuspend() {
    System.out.println("MontoThread.canSuspend()");
    return !isSuspended();
  }

  @Override
  public boolean isSuspended() {
    System.out.println("MontoThread.isSuspended()");
    // TODO
    return debugTarget.isSuspended();
  }

  @Override
  public void resume() throws DebugException {
    // TODO
    System.out.println("MontoThread.resume()");
  }

  @Override
  public void suspend() throws DebugException {
    // TODO
    System.out.println("MontoThread.suspend()");
  }

  @Override
  public boolean canStepInto() {
    System.out.println("MontoThread.canStepInto()");
    return true;
  }

  @Override
  public boolean canStepOver() {
    System.out.println("MontoThread.canStepOver()");
    return true;
  }

  @Override
  public boolean canStepReturn() {
    System.out.println("MontoThread.canStepReturn()");
    return false;
  }

  @Override
  public boolean isStepping() {
    System.out.println("MontoThread.isStepping()");
    // TODO
    return false;
  }

  @Override
  public void stepInto() throws DebugException {
    // TODO
    System.out.println("MontoThread.stepInto()");
  }

  @Override
  public void stepOver() throws DebugException {
    // TODO
    System.out.println("MontoThread.stepOver()");
  }

  @Override
  public void stepReturn() throws DebugException {
    // TODO
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoThread doesn't support step return"));
  }

  @Override
  public boolean canTerminate() {
    System.out.println("MontoThread.canTerminate()");
    return true;
  }

  @Override
  public boolean isTerminated() {
    System.out.println("MontoThread.isTerminated()");
    // TODO
    return false;
  }

  @Override
  public void terminate() throws DebugException {
    // TODO
    System.out.println("MontoThread.terminate()");
  }

  @Override
  public IStackFrame[] getStackFrames() throws DebugException {
    System.out.println("MontoThread.getStackFrames()");
    return stackFrames;
  }

  @Override
  public boolean hasStackFrames() throws DebugException {
    System.out.println("MontoThread.hasStackFrames()");
    return stackFrames.length > 0;
  }

  @Override
  public int getPriority() throws DebugException {
    System.out.println("MontoThread.getPriority()");
    return 0;
  }

  @Override
  public IStackFrame getTopStackFrame() throws DebugException {
    System.out.println("MontoThread.getTopStackFrame()");
    if (stackFrames.length > 0) {
      return stackFrames[0];
    }
    return null;
  }

  @Override
  public String getName() throws DebugException {
    System.out.println("MontoThread.getName()");
    return "MontoThread [" + name + "]";
  }

  @Override
  public IBreakpoint[] getBreakpoints() {
    System.out.println("MontoThread.getBreakpoints()");
    return suspendingBreakpoints;
  }

}
