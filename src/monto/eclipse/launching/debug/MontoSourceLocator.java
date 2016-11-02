package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

import monto.service.types.Source;

public class MontoSourceLocator implements IPersistableSourceLocator {

  @Override
  public Object getSourceElement(IStackFrame stackFrame) {
    if (stackFrame instanceof MontoStackFrame) {
      MontoStackFrame montoStackFrame = (MontoStackFrame) stackFrame;
      Source source = montoStackFrame.getStackFrame().getSource();
      IFile file =
          ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(source.getPhysicalName()));
      return file;
    } else {
      System.out.println("Unexpected stackframe class found in MontoSourceLocator: "
          + stackFrame.getClass().getName());
      return null;
    }
  }

  @Override
  public String getMemento() throws CoreException {
    return null;
  }

  @Override
  public void initializeFromMemento(String memento) throws CoreException {}

  @Override
  public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {}
}
