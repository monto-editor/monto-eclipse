package monto.eclipse.outline;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import monto.service.outline.Outline;


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

	private Image getImageInternal(Outline outline) {
		URL url = outline.getIcon().get();
		Image img = images.get(url.toString());
		if(img == null) {
			images.put(url.toString(), ImageDescriptor.createFromURL(url));
			img = images.get(url.toString());
		}
		return img;
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof Outline) {
			Outline outline = (Outline) element;
			if(outline.getIcon().isPresent())
				return getImageInternal(outline);
		} else if(element instanceof ModelTreeNode) {
			return getImage(((ModelTreeNode) element).getASTNode());
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof Outline) {
			Outline outline = (Outline) element;
			return outline.getLabel();
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
