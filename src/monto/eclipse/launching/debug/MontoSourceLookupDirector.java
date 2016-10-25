package monto.eclipse.launching.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class MontoSourceLookupDirector extends AbstractSourceLookupDirector {

  @Override
  public void initializeParticipants() {
    super.addParticipants(new ISourceLookupParticipant[] {new MontoSourceLookupParticipant()});
  }

  public static class MontoSourceLookupParticipant extends AbstractSourceLookupParticipant {

    @Override
    public String getSourceName(Object object) throws CoreException {
      if (object instanceof MontoStackFrame) {
        MontoStackFrame stackFrame = (MontoStackFrame) object;
        if (stackFrame.getStackFrame().getSource().isPresent()) {
          return stackFrame.getStackFrame().getSource().get().getPhysicalName();
        }
      }
      return null;
    }

  }

}
