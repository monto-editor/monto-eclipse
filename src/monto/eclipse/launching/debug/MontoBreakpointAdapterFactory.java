package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.IAdapterFactory;

public class MontoBreakpointAdapterFactory implements IAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    // TODO correct?
    if (adaptableObject.equals(MontoLineBreakpointAdapter.class)) {
      return (T) new MontoLineBreakpointAdapter();
    }
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    // TODO correct?
    return new Class<?>[] {MontoLineBreakpointAdapter.class};
  }

}
