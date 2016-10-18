package monto.eclipse.launching.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class MontoStackFrame extends MontoDebugElement implements IStackFrame {

  private MontoThread thread;
  private MontoVariable[] variables;
  private final String name;

  public MontoStackFrame(MontoDebugTarget debugTarget, String name) {
    super(debugTarget);
    this.name = name;
  }

  void _setThread(MontoThread thread) {
    this.thread = thread;
  }

  void _setVariables(MontoVariable[] variables) {
    this.variables = variables;
  }



  /* MODEL METHODS */

  @Override
  public IThread getThread() {
    return thread;
  }

  @Override
  public IVariable[] getVariables() throws DebugException {
    return variables;
  }

  @Override
  public boolean hasVariables() throws DebugException {
    return variables.length > 0;
  }

  @Override
  public int getLineNumber() throws DebugException {
    // TODO
    return -1;
  }

  @Override
  public int getCharStart() throws DebugException {
    return -1;
  }

  @Override
  public int getCharEnd() throws DebugException {
    return -1;
  }

  @Override
  public String getName() throws DebugException {
    return name;
  }

  @Override
  public IRegisterGroup[] getRegisterGroups() throws DebugException {
    return new IRegisterGroup[] {};
  }

  @Override
  public boolean hasRegisterGroups() throws DebugException {
    return false;
  }



  /* THREAD DELEGATE METHODS */

  @Override
  public boolean canStepInto() {
    return thread.canStepInto();
  }

  @Override
  public boolean canStepOver() {
    return thread.canStepOver();
  }

  @Override
  public boolean canStepReturn() {
    return thread.canStepReturn();
  }

  @Override
  public boolean isStepping() {
    return thread.isStepping();
  }

  @Override
  public void stepInto() throws DebugException {
    thread.stepInto();
  }

  @Override
  public void stepOver() throws DebugException {
    thread.stepOver();
  }

  @Override
  public void stepReturn() throws DebugException {
    thread.stepReturn();
  }

  @Override
  public boolean canResume() {
    return thread.canResume();
  }

  @Override
  public boolean canSuspend() {
    return thread.canSuspend();
  }

  @Override
  public boolean isSuspended() {
    return thread.isSuspended();
  }

  @Override
  public void resume() throws DebugException {
    thread.resume();
  }

  @Override
  public void suspend() throws DebugException {
    thread.suspend();
  }

  @Override
  public boolean canTerminate() {
    return thread.canTerminate();
  }

  @Override
  public boolean isTerminated() {
    return thread.isTerminated();
  }

  @Override
  public void terminate() throws DebugException {
    thread.terminate();
  }

}
