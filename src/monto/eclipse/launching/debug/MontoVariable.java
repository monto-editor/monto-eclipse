package monto.eclipse.launching.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class MontoVariable extends MontoDebugElement implements IVariable {

  private MontoValue value;
  private final String name;
  private final String type;

  public MontoVariable(MontoDebugTarget debugTarget, String name, String type) {
    super(debugTarget);
    this.name = name;
    this.type = type;
  }

  void _setValue(MontoValue value) {
    this.value = value;
  }

  @Override
  public void setValue(String expression) throws DebugException {
    notSupported("MontoVariable doesn't support modification", null);
  }

  @Override
  public void setValue(IValue value) throws DebugException {
    notSupported("MontoVariable doesn't support modification", null);
  }

  @Override
  public boolean supportsValueModification() {
    return false;
  }

  @Override
  public boolean verifyValue(String expression) throws DebugException {
    return false;
  }

  @Override
  public boolean verifyValue(IValue value) throws DebugException {
    return false;
  }

  @Override
  public IValue getValue() throws DebugException {
    return value;
  }

  @Override
  public String getName() throws DebugException {
    return name;
  }

  @Override
  public String getReferenceTypeName() throws DebugException {
    return type;
  }

  @Override
  public boolean hasValueChanged() throws DebugException {
    return false;
  }

}
