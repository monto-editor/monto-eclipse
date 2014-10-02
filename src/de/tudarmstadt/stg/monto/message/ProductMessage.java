package de.tudarmstadt.stg.monto.message;

import org.eclipse.imp.language.Language;

public class ProductMessage {
	private Source source;
	private Product product;
	private Language language;
	private Contents contents;
	
	public ProductMessage(Source source, Product product, Language language, Contents contents) {
		this.source = source;
		this.product = product;
		this.language = language;
		this.contents = contents;
	}
	
	public Source getSource() { return source; }
	public Product getProduct() { return product; }
	public Language getLanguage() { return language; }
	public Contents getContents() { return contents; }
}
