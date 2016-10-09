package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import monto.eclipse.Activator;

public class MontoLineBreakpointAdapter implements IToggleBreakpointsTarget {

  @Override
  public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException {
    if (part != null && part instanceof ITextEditor) {
      ITextEditor textEditor = (ITextEditor) part;
      IResource resource = (IResource) textEditor.getEditorInput().getAdapter(IResource.class);
      ITextSelection textSelection = (ITextSelection) selection;
      int lineNumber = textSelection.getStartLine();
      IBreakpoint[] oldBreakpoints =
          DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(Activator.PLUGIN_ID);
      if (oldBreakpoints.length > 0) {
        // delete them
        for (IBreakpoint oldBreakpoint : oldBreakpoints) {
          if (resource.equals(oldBreakpoint.getMarker().getResource())
              && ((ILineBreakpoint) oldBreakpoint).getLineNumber() == (lineNumber + 1)) {
            oldBreakpoint.delete();
          }
        }
      } else {
        // create new one
        DebugPlugin.getDefault().getBreakpointManager()
            .addBreakpoint(new MontoLineBreakpoint(resource, lineNumber + 1));
      }
    }
  }

  @Override
  public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
    return true;
  }

  @Override
  public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException {
    throw new DebugException(new Status(Status.ERROR, Activator.PLUGIN_ID,
        "MontoLineBreakPointAdapter doesn't support method breakpoints"));
  }

  @Override
  public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    return false;
  }

  @Override
  public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    throw new DebugException(new Status(Status.ERROR, Activator.PLUGIN_ID,
        "MontoLineBreakPointAdapter doesn't support watchpoints"));
  }

  @Override
  public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    return false;
  }
}
