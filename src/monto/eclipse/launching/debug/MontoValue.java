package monto.eclipse.launching.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class MontoValue extends MontoDebugElement implements IValue {

  private final String value;
  private MontoVariable[] variables;

  public MontoValue(MontoDebugTarget debugTarget, String value) {
    super(debugTarget);
    this.value = value;
  }
  
  void _setVariables(MontoVariable[] variables) {
    this.variables = variables;
  }

  @Override
  public String getReferenceTypeName() throws DebugException {
    return variables[0].getReferenceTypeName();
    System.out.println("MontoValue.getReferenceTypeName()");
  }

  @Override
  public String getValueString() throws DebugException {
    System.out.println("MontoValue.getValueString()");
    return value;
  }

  @Override
  public boolean isAllocated() throws DebugException {
    System.out.println("MontoValue.isAllocated()");
    return true;
  }

  @Override
  public IVariable[] getVariables() throws DebugException {
    System.out.println("MontoValue.getVariables()");
    return variables;
  }

  @Override
  public boolean hasVariables() throws DebugException {
    return true;
    System.out.println("MontoValue.hasVariables()");
  }

}
