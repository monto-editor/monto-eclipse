package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

import monto.eclipse.Activator;

public class MontoLineBreakpoint extends LineBreakpoint {

  public MontoLineBreakpoint(IResource resource, int lineNumber) throws CoreException {
    // run(getMarkerRule(resource), (IProgressMonitor monitor) -> {
    // IMarker marker = resource.createMarker("example.debug.core.pda.markerType.lineBreakpoint");
    // setMarker(marker);
    // marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
    // marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
    // marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
    // marker.setAttribute(IMarker.MESSAGE,
    // "Line Breakpoint: " + resource.getName() + " [line: " + lineNumber + "]");
    // });
    IMarker marker = resource.createMarker("monto.eclipse.launching.debug.lineBreakpoint.marker");
    setMarker(marker);
    setEnabled(true);
    ensureMarker().setAttribute(IMarker.LINE_NUMBER, lineNumber);
    ensureMarker().setAttribute(IBreakpoint.ID, getModelIdentifier());
  }

  @Override
  public String getModelIdentifier() {
    return Activator.PLUGIN_ID;
  }
}
