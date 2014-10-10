package de.tudarmstadt.stg.monto.message;

import java.io.Reader;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
	
	public static ProductMessage decode(Reader reader) throws ProductMessageParseException {
		try {
			JSONObject message = (JSONObject) JSONValue.parse(reader);
			Source source = new Source((String) message.get("source"));
			Product product = new Product((String) message.get("product"));
			Language language = LanguageRegistry.findLanguage((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			return new ProductMessage(source, product, language, contents);
		} catch (Exception e) {
			throw new ProductMessageParseException(e);
		}
	}
}
