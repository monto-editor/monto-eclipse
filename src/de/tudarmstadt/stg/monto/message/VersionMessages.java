package de.tudarmstadt.stg.monto.message;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class VersionMessages {

	@SuppressWarnings("unchecked")
	public static VersionMessage decode(Reader reader) throws ParseException {
		try {
			JSONObject message = (JSONObject) JSONValue.parse(reader);
			Long id = (Long) message.get("id");
			Source source = new Source((String) message.get("source"));
			Language language = new Language((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			List<Selection> selections = new ArrayList<>();
			JSONArray selectionsArray = (JSONArray) message.getOrDefault("selections", new JSONArray());
			Iterator<JSONObject> iterator = selectionsArray.iterator();
			while(iterator.hasNext()) {
				JSONObject selection = iterator.next();
				Long begin = (Long) selection.get("begin");
				Long end = (Long) selection.get("end");
				selections.add(new Selection(begin.intValue(), end.intValue() - begin.intValue()));
			}
			return new VersionMessage(new LongKey(id), source, language, contents, selections);
		} catch (Exception e) {
			throw new ParseException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encode(VersionMessage message) {
		JSONObject version = new JSONObject();
		version.put("id", message.getId().longValue());
		version.put("source", message.getSource().toString());
		version.put("language", message.getLanguage().toString());
		version.put("contents", message.getContent().toString());
		JSONArray selections = new JSONArray();
		for(Selection selection : message.getSelections()) {
			JSONObject sel = new JSONObject();
			sel.put("begin", selection.getStartOffset());
			sel.put("end", selection.getEndOffset());
			selections.add(sel);
		}
		version.put("selections", selections);
		return version;
	}

}
