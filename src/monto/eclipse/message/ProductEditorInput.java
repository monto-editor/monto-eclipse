package monto.eclipse.message;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import monto.service.message.Language;
import monto.service.message.Product;
import monto.service.message.Source;

public class ProductEditorInput implements IEditorInput {

	private Source source;
	private Product product;
	private Language language;
	
	public ProductEditorInput(Source source, Product product,
			Language language) {
		this.source = source;
		this.product = product;
		this.language = language;
	}

	public Source getSource() {
		return source;
	}

	public Product getProduct() {
		return product;
	}
	
	public Language getLanguage() {
		return language;
	}

	@Override
	public String getName() {
		return String.format("%s - %s - %s", source.toString(), product.toString(), language.toString());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
