package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

import monto.eclipse.Activator;

public class MontoEclipseDebugElement implements IDebugElement {
  protected final MontoEclipseDebugTarget debugTarget;
  
  public MontoEclipseDebugElement(MontoEclipseDebugTarget debugTarget) {
    this.debugTarget = debugTarget;
  }
  
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    return Adapters.adapt(this, adapter);
  }

  @Override
  public String getModelIdentifier() {
    return Activator.PLUGIN_ID;
  }

  @Override
  public IDebugTarget getDebugTarget() {
    return debugTarget;
  }

  @Override
  public ILaunch getLaunch() {
    return debugTarget.getLaunch();
  }
}
