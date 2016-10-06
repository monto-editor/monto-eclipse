package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class MontoEclipseThread extends MontoEclipseDebugElement implements IThread {

  private final String name;
  private MontoEclipseStackFrame[] stackFrames;

  public MontoEclipseThread(MontoEclipseDebugTarget debugTarget, String name) {
    super(debugTarget);
    this.name = name;
  }
  
  void _setStackFrames(MontoEclipseStackFrame[] stackFrames) {
    this.stackFrames = stackFrames;
  }

  @Override
  public boolean canResume() {
    return true;
  }

  @Override
  public boolean canSuspend() {
    return true;
  }

  @Override
  public boolean isSuspended() {
    // TODO
    return false;
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
    return true;
  }

  @Override
  public boolean canStepOver() {
    return true;
  }

  @Override
  public boolean canStepReturn() {
    return false;
  }

  @Override
  public boolean isStepping() {
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
    return true;
  }

  @Override
  public boolean isTerminated() {
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
    return stackFrames[0];
  }

  @Override
  public String getName() throws DebugException {
    return name;
  }

  @Override
  public IBreakpoint[] getBreakpoints() {
    // TODO return breakpoint, that suspended this thread
    return new IBreakpoint[] {};
  }


}
