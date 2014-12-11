package de.tudarmstadt.stg.monto.json;

import java.io.Reader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.tonian.director.dm.json.JSONWriter;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.server.AbstractServer;
import de.tudarmstadt.stg.monto.server.ProductMessageListener;

public class JsonPrettyPrinter extends AbstractServer implements ProductMessageListener {

	private String prettyPrint(Reader reader) {
		Object obj = JSONValue.parse(reader);
		JSONWriter writer = new JSONWriter();
		try {
			if(obj instanceof JSONObject) {
				((JSONObject) obj).writeJSONString(writer);
			} else if(obj instanceof JSONArray) {
				((JSONArray) obj).writeJSONString(writer);
			}
		} catch(Exception e) {
			
		}
		return writer.toString();
	}

	@Override
	public void onProductMessage(ProductMessage message) {
		if(message.getLanguage().equals(Languages.json)) {
			
			Activator.getProfiler().start(JsonPrettyPrinter.class, "onVersionMessage", message);
			Contents content = new StringContent(prettyPrint(message.getContents().getReader()));
			Activator.getProfiler().end(JsonPrettyPrinter.class, "onVersionMessage", message);
			
			emitProductMessage(
					new ProductMessage(
							message.getVersionId(),
							new LongKey(1),
							message.getSource(),
							message.getProduct(),
							Languages.jsonPretty,
							content));
		}
	}
	
	@Override
	protected boolean isRelveant(VersionMessage message) {
		return false;
	}

	@Override
	protected void receiveVersionMessage(VersionMessage message) {

	}
	
}
