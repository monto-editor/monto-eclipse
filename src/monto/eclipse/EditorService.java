package monto.eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.base.EditorServiceBase;

public class EditorService extends EditorServiceBase {

  @Override
  public AnalysisRequired getAnalysisRequired() {
    return AnalysisRequired.NONE;
  }

  @Override
  public void update(IParseController parseController, IProgressMonitor monitor) {
    if (parseController instanceof MontoParseController) {
      MontoParseController controller = (MontoParseController) parseController;
      controller.setEditor(this.editor);
    }
  }

}
