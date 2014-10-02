package de.tudarmstadt.stg.monto.message;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ProductEditorInput implements IEditorInput {

	private Source source;
	private Product product;

	public ProductEditorInput(Source source, Product product) {
		this.source = source;
		this.product = product;
	}
	
	public Source getSource() {
		return source;
	}

	public Product getProduct() {
		return product;
	}

	@Override
	public String getName() {
		return String.format("%s - %s", source.toString(), product.toString());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

}
