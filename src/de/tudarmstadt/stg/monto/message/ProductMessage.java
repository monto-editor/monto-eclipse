package de.tudarmstadt.stg.monto.message;


public class ProductMessage implements Message {
	
	private LongKey id;
	private Source source;
	private Product product;
	private Language language;
	private Contents contents;
	
	public ProductMessage(LongKey id,Source source, Product product, Language language, Contents contents) {
		this.id = id;
		this.source = source;
		this.product = product;
		this.language = language;
		this.contents = contents;
	}
	
	public LongKey getId() { return id; }
	public Source getSource() { return source; }
	public Product getProduct() { return product; }
	public Language getLanguage() { return language; }
	public Contents getContents() { return contents; }
	
	@Override
	public String toString() {
		return String.format("{ source: %s, product: %s, language: %s, contents: %s }", source, product, language, contents);
	}
}
