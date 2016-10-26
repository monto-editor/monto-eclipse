package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class MontoDebugModelPresentation extends LabelProvider implements IDebugModelPresentation {

  @Override
  public void setAttribute(String attribute, Object value) {}

  @Override
  public Image getImage(Object element) {
    // Use default image
    return null;
  }

  @Override
  public String getText(Object element) {
    // Use default text
    return null;
  }

  @Override
  public void computeDetail(IValue value, IValueDetailListener listener) {
    // Use default
    listener.detailComputed(value, null);
  }

  @Override
  public IEditorInput getEditorInput(Object element) {
    if (element instanceof IFile) {
      return new FileEditorInput((IFile) element);
    } else if (element instanceof ILineBreakpoint) {
      return new FileEditorInput((IFile) ((ILineBreakpoint) element).getMarker().getResource());
    } else {
      System.err.printf("Unexpected class in MontoDebugModelPresentation.getEditorInput(%s)\n",
          element.getClass().getName());
      return null;
    }
  }

  @Override
  public String getEditorId(IEditorInput input, Object element) {
    if (element instanceof IFile || element instanceof MontoLineBreakpoint) {
      return UniversalEditor.EDITOR_ID;
    } else {
      System.err.printf("Unexpected class in MontoDebugModelPresentation.getEditorId(%s, %s)\n",
          input.getClass().getName(), element.getClass().getName());
      return null;
    }
  }

}
