package de.tudarmstadt.stg.monto.message;

import java.io.Reader;

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
	
	public static ProductMessage decode(Reader reader) throws ParseException {
		try {
			JSONObject message = (JSONObject) JSONValue.parse(reader);
			Source source = new Source((String) message.get("source"));
			Product product = new Product((String) message.get("product"));
			Language language = new Language((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			return new ProductMessage(source, product, language, contents);
		} catch (Exception e) {
			throw new ParseException(e);
		}
	}
	
	@Override
	public String toString() {
		return String.format("{ source: %s, product: %s, language: %s, contents: %s }", source, product, language, contents);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encode(ProductMessage msg) {
		JSONObject jsonMessage = new JSONObject();
		jsonMessage.put("source", msg.getSource().toString());
		jsonMessage.put("product", msg.getProduct().toString());
		jsonMessage.put("language", msg.getLanguage().toString());
		jsonMessage.put("contents", msg.getContents().toString());
		return jsonMessage;
	}
}
