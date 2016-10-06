package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.Status;
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

  @Override
  public boolean canStepInto() {
    return false;
  }

  @Override
  public boolean canStepOver() {
    return false;
  }

  @Override
  public boolean canStepReturn() {
    return false;
  }

  @Override
  public boolean isStepping() {
    return false;
  }

  @Override
  public void stepInto() throws DebugException {
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoStackFrame doesn't support stepping"));
  }

  @Override
  public void stepOver() throws DebugException {
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoStackFrame doesn't support stepping"));
  }

  @Override
  public void stepReturn() throws DebugException {
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoStackFrame doesn't support stepping"));
  }

  @Override
  public boolean canResume() {
    return false;
  }

  @Override
  public boolean canSuspend() {
    return false;
  }

  @Override
  public boolean isSuspended() {
    return false;
  }

  @Override
  public void resume() throws DebugException {
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoStackFrame doesn't support suspending"));
  }

  @Override
  public void suspend() throws DebugException {
    throw new DebugException(new Status(Status.ERROR, getModelIdentifier(),
        "MontoStackFrame doesn't support suspending"));
  }

  @Override
  public boolean canTerminate() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public void terminate() throws DebugException {
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoStackFrame doesn't termination"));
  }

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
    // TODO
    return -1;
  }

  @Override
  public int getCharEnd() throws DebugException {
    // TODO
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

}
