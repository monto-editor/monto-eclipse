package monto.eclipse.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

public class MontoStreamMonitor implements IStreamMonitor {
  private List<IStreamListener> listeners;
  private StringBuffer contentsBuffer;

  public MontoStreamMonitor() {
    listeners = new ArrayList<>();
    contentsBuffer = new StringBuffer();
  }

  @Override
  public void addListener(IStreamListener listener) {
    listeners.add(listener);
  }

  @Override
  public String getContents() {
    return contentsBuffer.toString();
  }

  @Override
  public void removeListener(IStreamListener listener) {
    listeners.remove(listener);
  }

  protected void fireEvent(String newText) {
    contentsBuffer.append(newText);
    for (IStreamListener listener : listeners) {
      listener.streamAppended(newText, this);
    }
  }
}
