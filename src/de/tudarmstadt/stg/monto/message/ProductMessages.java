package de.tudarmstadt.stg.monto.message;

import java.io.Reader;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.Activator;

public class ProductMessages {

	public static ProductMessage decode(Reader reader) throws ParseException {
		JSONObject message = (JSONObject) JSONValue.parse(reader);
		return decode(message);
	}
	
	@SuppressWarnings("unchecked")
	public static ProductMessage decode(JSONObject message) throws ParseException {
		try {
			long start = System.nanoTime();
			Long versionId = (Long) message.get("version_id");
			Long productId = (Long) message.get("product_id");
			Source source = new Source((String) message.get("source"));
			Product product = new Product((String) message.get("product"));
			Language language = new Language((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			List<Dependency> invalid = Dependencies.decode((JSONArray) message.getOrDefault("invalid", new JSONArray()));
			List<Dependency> dependencies = Dependencies.decode((JSONArray) message.getOrDefault("dependencies", new JSONArray()));
			ProductMessage msg = new ProductMessage(
					new LongKey(versionId),
					new LongKey(productId),
					source,
					product,
					language,
					contents,
					invalid,
					dependencies);
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
		JSONObject encoding = new JSONObject();
		encoding.put("version_id", msg.getVersionId().longValue());
		encoding.put("product_id", msg.getProductId().longValue());
		encoding.put("source", msg.getSource().toString());
		encoding.put("product", msg.getProduct().toString());
		encoding.put("language", msg.getLanguage().toString());
		encoding.put("contents", msg.getContents().toString());
		encoding.put("invalid", Dependencies.encode(msg.getInvalid()));
		encoding.put("dependencies", Dependencies.encode(msg.getDependencies()));
		Activator.getProfiler().end(ProductMessage.class, "encode", msg);
		return encoding;
	}

}
