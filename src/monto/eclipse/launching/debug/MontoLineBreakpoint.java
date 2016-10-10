package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

import monto.eclipse.Activator;
import monto.service.types.Source;

public class MontoLineBreakpoint extends LineBreakpoint {
  public static final String MARKER_ATTRIBUTE_PHYSICAL_SOURCE_NAME =
      "montoMarkerKeyPhysicalSourceName";
  public static final String MARKER_ATTRIBUTE_LOGICAL_SOURCE_NAME =
      "montoMarkerKeyLogicalSourceName";

  // Eclipse tries to create an instance of this class on IDE launch, if a MontoLineBreakpoint was
  // persistent in a preceding launch. For that to be successful, there needs to be a constructor
  // with no parameter. Eclipse then probably attaches the persisted marker data to this instance.
  public MontoLineBreakpoint() {}

  public MontoLineBreakpoint(IResource resource, int lineNumber, Source source)
      throws CoreException {
    IMarker marker = resource.createMarker("monto.eclipse.launching.debug.lineBreakpoint.marker");
    setMarker(marker);
    setEnabled(true);
    marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
    marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
    writeSourceToMarker(marker, source);
    marker.setAttribute(IMarker.MESSAGE, toString());
    marker.setAttribute(IMarker.LOCATION, toString());
  }

  @Override
  public String getModelIdentifier() {
    return Activator.PLUGIN_ID;
  }

  private void writeSourceToMarker(IMarker marker, Source source) throws CoreException {
    marker.setAttribute(MontoLineBreakpoint.MARKER_ATTRIBUTE_PHYSICAL_SOURCE_NAME,
        source.getPhysicalName());
    marker.setAttribute(MontoLineBreakpoint.MARKER_ATTRIBUTE_LOGICAL_SOURCE_NAME,
        source.getLogicalName().orElse(null));
  }

  public Source getSource() throws DebugException {
    IMarker marker = ensureMarker();
    // reconstruct Source from marker
    String logicalSourceName =
        marker.getAttribute(MontoLineBreakpoint.MARKER_ATTRIBUTE_LOGICAL_SOURCE_NAME, null);
    String physicalSourceName =
        marker.getAttribute(MontoLineBreakpoint.MARKER_ATTRIBUTE_PHYSICAL_SOURCE_NAME, null);
    return new Source(physicalSourceName, logicalSourceName);
  }

  public int getLineNumber() throws DebugException {
    return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
  }

  @Override
  public String toString() {
    try {
      return String.format("MontoLineBreakpoint {source: %s, lineNumber: %d}", getSource(),
          getLineNumber());
    } catch (DebugException e) {
      return "MontoLineBreakpoint {unknownLocation}";
    }
  }
}
