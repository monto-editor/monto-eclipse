package de.tudarmstadt.stg.monto.json;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.tonian.director.dm.json.JSONWriter;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.connection.AbstractServer;
import de.tudarmstadt.stg.monto.connection.Pair;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;

public class JsonPrettyPrinter extends AbstractServer {

	public JsonPrettyPrinter(Pair connection) {
		super(connection);
	}

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
	public Either<Exception,ProductMessage> onMessage(List<Message> messages) {
		Optional<ProductMessage> opt = messages.stream().filter(m -> m instanceof ProductMessage).map(m -> {
			ProductMessage message = (ProductMessage) m;
			Activator.getProfiler().start(JsonPrettyPrinter.class, "onVersionMessage", message);
			Contents content = new StringContent(prettyPrint(message.getContents().getReader()));
			Activator.getProfiler().end(JsonPrettyPrinter.class, "onVersionMessage", message);
			
			return new ProductMessage(
				message.getVersionId(),
				new LongKey(1),
				message.getSource(),
				message.getProduct(),
				Languages.jsonPretty,
				content);
		}).findAny();
		
		if(opt.isPresent()) {
			return Either.right(opt.get());
		} else {
			return Either.left(new IllegalArgumentException("Messages didn't contain product message to convert"));
		}
	}	
}
