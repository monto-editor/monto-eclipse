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
    System.out.println("MontoStackFrame.canStepInto()");
    return false;
  }

  @Override
  public boolean canStepOver() {
    System.out.println("MontoStackFrame.canStepOver()");
    return false;
  }

  @Override
  public boolean canStepReturn() {
    System.out.println("MontoStackFrame.canStepReturn()");
    return false;
  }

  @Override
  public boolean isStepping() {
    System.out.println("MontoStackFrame.isStepping()");
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
    System.out.println("MontoStackFrame.canResume()");
    return false;
  }

  @Override
  public boolean canSuspend() {
    System.out.println("MontoStackFrame.canSuspend()");
    return false;
  }

  @Override
  public boolean isSuspended() {
    System.out.println("MontoStackFrame.isSuspended()");
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
    System.out.println("MontoStackFrame.canTerminate()");
    return false;
  }

  @Override
  public boolean isTerminated() {
    System.out.println("MontoStackFrame.isTerminated()");
    return false;
  }

  @Override
  public void terminate() throws DebugException {
    throw new DebugException(
        new Status(Status.ERROR, getModelIdentifier(), "MontoStackFrame doesn't termination"));
  }

  @Override
  public IThread getThread() {
    System.out.println("MontoStackFrame.getThread()");
    return thread;
  }

  @Override
  public IVariable[] getVariables() throws DebugException {
    System.out.println("MontoStackFrame.getVariables()");
    return variables;
  }

  @Override
  public boolean hasVariables() throws DebugException {
    System.out.println("MontoStackFrame.hasVariables()");
    return variables.length > 0;
  }

  @Override
  public int getLineNumber() throws DebugException {
    System.out.println("MontoStackFrame.getLineNumber()");
    // TODO
    return -1;
  }

  @Override
  public int getCharStart() throws DebugException {
    System.out.println("MontoStackFrame.getCharStart()");
    // TODO
    return -1;
  }

  @Override
  public int getCharEnd() throws DebugException {
    System.out.println("MontoStackFrame.getCharEnd()");
    // TODO
    return -1;
  }

  @Override
  public String getName() throws DebugException {
    System.out.println("MontoStackFrame.getName()");
    return name;
  }

  @Override
  public IRegisterGroup[] getRegisterGroups() throws DebugException {
    System.out.println("MontoStackFrame.getRegisterGroups()");
    return new IRegisterGroup[] {};
  }

  @Override
  public boolean hasRegisterGroups() throws DebugException {
    System.out.println("MontoStackFrame.hasRegisterGroups()");
    return false;
  }

}
