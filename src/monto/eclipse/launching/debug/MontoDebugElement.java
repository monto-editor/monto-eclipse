package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

import monto.eclipse.Activator;

public class MontoDebugElement extends PlatformObject implements IDebugElement {
  protected MontoDebugTarget debugTarget;

  public MontoDebugElement(MontoDebugTarget debugTarget) {
    this.debugTarget = debugTarget;
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    System.out.printf("MontoDebugElement.getAdapter(%s)\n", adapter);
    return super.getAdapter(adapter);
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

  public void fireEvent(int eventKindId) {
    fireEvent(eventKindId, DebugEvent.UNSPECIFIED);
  }

  public void fireEvent(int eventKindId, int eventDetailId) {
    DebugPlugin.getDefault()
        .fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, eventKindId, eventDetailId)});
  }
}
