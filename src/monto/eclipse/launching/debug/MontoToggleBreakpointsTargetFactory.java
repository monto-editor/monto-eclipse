package monto.eclipse.launching.debug;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public class MontoToggleBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {

  @Override
  public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
    if (part instanceof UniversalEditor) {
      return Collections.singleton("monto.eclipse.launching.debug.toggleBreakpointsTargetFactory");
    } else {
      return new HashSet<>();
    }
  }

  @Override
  public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
    return "monto.eclipse.launching.debug.toggleBreakpointsTargetFactory";
  }

  @Override
  public IToggleBreakpointsTarget createToggleTarget(String targetID) {
    return new MontoToggleBreakpointsTarget();
  }

  @Override
  public String getToggleTargetName(String targetID) {
    System.out.println("MontoToggleBreakpointsTargetFactory.getToggleTargetName()");
    System.out.println("targetID: " + targetID);
    // TODO never called
    return "monto.eclipse.launching.debug.toggleBreakpointsTargetFactory";
  }

  @Override
  public String getToggleTargetDescription(String targetID) {
    System.out.println("MontoToggleBreakpointsTargetFactory.getToggleTargetDescription()");
    System.out.println("targetID: " + targetID);
    // TODO never called
    return "monto.eclipse.launching.debug.toggleBreakpointsTargetFactory";
  }

}
