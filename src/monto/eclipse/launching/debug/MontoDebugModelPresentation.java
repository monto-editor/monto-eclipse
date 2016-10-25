package monto.eclipse.launching.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import monto.eclipse.outline.LabelProvider;
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
//    String detail = "";
//    try {
//      detail = value.getValueString();
//    } catch (DebugException e) {
//    }
    // Use default
    listener.detailComputed(value, null);
  }

  @Override
  public IEditorInput getEditorInput(Object element) {
    if (element instanceof IFile) {
      return new FileEditorInput((IFile) element);
    }
    if (element instanceof ILineBreakpoint) {
      return new FileEditorInput((IFile) ((ILineBreakpoint) element).getMarker().getResource());
    }
    return null;
  }

  @Override
  public String getEditorId(IEditorInput input, Object element) {
    if (element instanceof IFile || element instanceof ILineBreakpoint) {
      return UniversalEditor.EDITOR_ID;
    }
    return null;
  }

}
