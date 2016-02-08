package monto.eclipse.outline;

import org.eclipse.imp.services.base.TreeModelBuilderBase;

import monto.service.outline.Outline;

public class TreeModelBuilder extends TreeModelBuilderBase {

	@Override
	protected void visitTree(Object obj) {
		Outline result = (Outline) obj;
		
		if(result == null)
			return;
		
		result.getChildren().forEach(child -> buildOutline(child));
	}
	
	private void buildOutline(Outline out) {
		if(out.isLeaf()) {
			createSubItem(out);
		} else {
			pushSubItem(out);
			out.getChildren().forEach(child ->
					buildOutline(child));
			popSubItem();
		}
	}
}
