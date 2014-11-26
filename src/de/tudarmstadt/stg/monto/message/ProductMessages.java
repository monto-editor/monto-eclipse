package de.tudarmstadt.stg.monto.message;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.Activator;

public class ProductMessages {

	public static ProductMessage decode(Reader reader) throws ParseException {
		try {
			long start = System.nanoTime();
			JSONObject message = (JSONObject) JSONValue.parse(reader);
			Long id = (Long) message.get("id");
			Source source = new Source((String) message.get("source"));
			Product product = new Product((String) message.get("product"));
			Language language = new Language((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			ProductMessage msg = new ProductMessage(new LongKey(id),source, product, language, contents);
			Activator.getProfiler().start(ProductMessage.class, "decode_"+msg.getProduct()+"_"+msg.getLanguage(), msg, start);
			Activator.getProfiler().end(ProductMessage.class, "decode_"+msg.getProduct()+"_"+msg.getLanguage(), msg);
			return msg;
		} catch (Exception e) {
			throw new ParseException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encode(ProductMessage msg) {
		Activator.getProfiler().start(ProductMessage.class, "encode", msg);
		JSONObject jsonMessage = new JSONObject();
		jsonMessage.put("id", msg.getId().longValue());
		jsonMessage.put("source", msg.getSource().toString());
		jsonMessage.put("product", msg.getProduct().toString());
		jsonMessage.put("language", msg.getLanguage().toString());
		jsonMessage.put("contents", msg.getContents().toString());
		Activator.getProfiler().end(ProductMessage.class, "encode", msg);
		return jsonMessage;
	}

}
