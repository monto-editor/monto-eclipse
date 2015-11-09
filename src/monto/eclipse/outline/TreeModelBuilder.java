package monto.eclipse.outline;

import org.eclipse.imp.services.base.TreeModelBuilderBase;

import monto.eclipse.ParseResult;
import monto.service.outline.Outline;

public class TreeModelBuilder extends TreeModelBuilderBase {

	@Override
	protected void visitTree(Object obj) {
		ParseResult result = (ParseResult) obj;
		
		if(result == null || result.getOutline() == null)
			return;
		
		result.getOutline().getChildren().forEach(child -> buildOutline(child, result.getDocument()));
	}
	
	private void buildOutline(Outline out,String document) {
		if(out.isLeaf()) {
			createSubItem(new OutlineLabel(out, document));
		} else {
			pushSubItem(new OutlineLabel(out, document));
			out.getChildren().forEach(child ->
					buildOutline(child, document));
			popSubItem();
		}
	}
}
