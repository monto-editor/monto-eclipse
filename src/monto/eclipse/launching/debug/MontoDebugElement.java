package monto.eclipse.launching.debug;

import org.eclipse.debug.core.model.DebugElement;

import monto.eclipse.Activator;

public class MontoDebugElement extends DebugElement {

  public MontoDebugElement(MontoDebugTarget debugTarget) {
    super(debugTarget);
  }

  @Override
  public MontoDebugTarget getDebugTarget() {
    return (MontoDebugTarget) super.getDebugTarget();
  }

  @Override
  public String getModelIdentifier() {
    return Activator.PLUGIN_ID;
  }
}
