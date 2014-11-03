package de.tudarmstadt.stg.monto.outline;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class LabelProvider implements ILabelProvider {
	
	private Set<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();
	ISharedImages images = JavaUI.getSharedImages();

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}


	@Override
	public Image getImage(Object element) {
		if(element instanceof OutlineLabel) {
			OutlineLabel label = (OutlineLabel) element;
			if(label.getOutline().getIcon().isPresent())
				return images.getImage(label.getOutline().getIcon().get());
		} else if(element instanceof ModelTreeNode) {
			return getImage(((ModelTreeNode) element).getASTNode());
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof OutlineLabel) {
			OutlineLabel label = (OutlineLabel) element;
			return label.getText();
		} else if(element instanceof ModelTreeNode) {
			return getText(((ModelTreeNode) element).getASTNode());
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

}
