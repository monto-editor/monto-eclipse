package monto.eclipse.launching.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class MontoEclipseValue extends MontoEclipseDebugElement implements IValue {

  private final String value;
  private MontoEclipseVariable[] variables;

  public MontoEclipseValue(MontoEclipseDebugTarget debugTarget, String value) {
    super(debugTarget);
    this.value = value;
  }
  
  void _setVariables(MontoEclipseVariable[] variables) {
    this.variables = variables;
  }

  @Override
  public String getReferenceTypeName() throws DebugException {
    return variables[0].getReferenceTypeName();
  }

  @Override
  public String getValueString() throws DebugException {
    return value;
  }

  @Override
  public boolean isAllocated() throws DebugException {
    return true;
  }

  @Override
  public IVariable[] getVariables() throws DebugException {
    return variables;
  }

  @Override
  public boolean hasVariables() throws DebugException {
    return true;
  }

}
