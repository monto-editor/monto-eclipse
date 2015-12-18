package monto.eclipse.outline;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class LabelProvider implements ILabelProvider {
	
	private Set<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();
	ImageRegistry images = new ImageRegistry(Display.getCurrent()); 

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	private Image getImageInternal(String filepath) {
		Image img = images.get(filepath);
		if(img == null) {
			images.put(filepath, new Image(Display.getCurrent(),filepath));
			return images.get(filepath);
		} else {
			return img;
		}
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof OutlineLabel) {
			OutlineLabel label = (OutlineLabel) element;
			if(label.getOutline().getIcon().isPresent())
				return getImageInternal(label.getOutline().getIcon().get());
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
