package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import monto.eclipse.Activator;
import monto.eclipse.MontoParseController;

public class MontoToggleBreakpointsTarget implements IToggleBreakpointsTarget {

  @Override
  public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException {
    System.out.println("MontoToggleBreakpointsTarget.toggleLineBreakpoints()");
    if (part != null && part instanceof UniversalEditor) {
      UniversalEditor universalEditor = (UniversalEditor) part;
      if (universalEditor.getParseController() instanceof MontoParseController) {
        MontoParseController montoParseController =
            (MontoParseController) universalEditor.getParseController();

        IResource resource =
            (IResource) universalEditor.getEditorInput().getAdapter(IResource.class);
        ITextSelection textSelection = (ITextSelection) selection;
        int lineNumber = textSelection.getStartLine();

        // delete old breakpoints in lineNumber
        IBreakpoint[] oldBreakpoints =
            DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(Activator.PLUGIN_ID);
        boolean deletedBreakPoints = false;
        for (IBreakpoint oldBreakpoint : oldBreakpoints) {
          if (resource.equals(oldBreakpoint.getMarker().getResource())
              && ((ILineBreakpoint) oldBreakpoint).getLineNumber() == (lineNumber + 1)) {
            deletedBreakPoints = true;
            oldBreakpoint.delete();
          }
        }

        if (!deletedBreakPoints) {
          // create new one
          DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(
              new MontoLineBreakpoint(resource, lineNumber + 1, montoParseController.getSource()));
        }
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
        "MontoToggleBreakpointsTarget doesn't support method breakpoints"));
  }

  @Override
  public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    return false;
  }

  @Override
  public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    throw new DebugException(new Status(Status.ERROR, Activator.PLUGIN_ID,
        "MontoToggleBreakpointsTarget doesn't support watchpoints"));
  }

  @Override
  public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    return false;
  }
}
