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

  
  
  /* MODEL METHODS */

  @Override
  public IStackFrame[] getStackFrames() throws DebugException {
    return stackFrames;
  }

  @Override
  public boolean hasStackFrames() throws DebugException {
    return stackFrames.length > 0;
  }

  @Override
  public int getPriority() throws DebugException {
    return 0;
  }

  @Override
  public IStackFrame getTopStackFrame() throws DebugException {
    if (stackFrames.length > 0) {
      return stackFrames[0];
    }
    return null;
  }

  @Override
  public String getName() throws DebugException {
    return "MontoThread [" + name + "]";
  }

  @Override
  public IBreakpoint[] getBreakpoints() {
    return suspendingBreakpoints;
  }



  /* DEBUG TARGET DELEGATE METHODS */

  @Override
  public boolean canResume() {
    return debugTarget.canResume();
  }

  @Override
  public boolean canSuspend() {
    return debugTarget.canSuspend();
  }

  @Override
  public boolean isSuspended() {
    return debugTarget.isSuspended();
  }

  @Override
  public void resume() throws DebugException {
    debugTarget.resume();
  }

  @Override
  public void suspend() throws DebugException {
    debugTarget.suspend();
  }

  @Override
  public boolean canTerminate() {
    return debugTarget.canTerminate();
  }

  @Override
  public boolean isTerminated() {
    return debugTarget.isTerminated();
  }

  @Override
  public void terminate() throws DebugException {
    debugTarget.terminate();
  }



  /* STEPPING METHODS */

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

}
