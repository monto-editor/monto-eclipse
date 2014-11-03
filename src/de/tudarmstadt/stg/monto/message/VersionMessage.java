package de.tudarmstadt.stg.monto.message;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class VersionMessage {

	private Source source;
	private Contents content;
	private Language language;
	private List<Selection> selections;

	public VersionMessage(Source source, Language language, Contents content, Selection ... selections) {
		this(source,language,content,Arrays.asList(selections));
	}
	
	public VersionMessage(Source source, Language language, Contents content, List<Selection> selections) {
		this.source = source;
		this.language = language;
		this.content = content;
		this.selections = selections;
	}

	public Source getSource() {
		return source;
	}

	public Contents getContent() {
		return content;
	}

	public Language getLanguage() {
		return language;
	}

	public List<Selection> getSelections() {
		return selections;
	}
	
	
	@SuppressWarnings("unchecked")
	public static JSONObject encode(VersionMessage message) {
		JSONObject version = new JSONObject();
		version.put("source", message.getSource().toString());
		version.put("language", message.getLanguage().toString());
		version.put("contents", message.getContent().toString());
		JSONArray selections = new JSONArray();
		for(Selection selection : message.getSelections()) {
			JSONObject sel = new JSONObject();
			sel.put("begin", selection.getBegin());
			sel.put("end", selection.getEnd());
			selections.add(sel);
		}
		version.put("selections", selections);
		return version;
	}

	public static VersionMessage decode(Reader reader) throws ParseException {
		try {
			JSONObject message = (JSONObject) JSONValue.parse(reader);
			Source source = new Source((String) message.get("source"));
			Language language = new Language((String) message.get("language"));
			Contents contents = new StringContent((String) message.get("contents"));
			List<Selection> selections = new ArrayList<>();
			JSONArray selectionsArray = (JSONArray) message.get("selections");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = selectionsArray.iterator();
			while(iterator.hasNext()) {
				JSONObject selection = iterator.next();
				Long begin = (Long) selection.get("begin");
				Long end = (Long) selection.get("end");
				selections.add(new Selection(begin.intValue(), end.intValue()));
			}
			return new VersionMessage(source, language, contents, selections);
		} catch (Exception e) {
			throw new ParseException(e);
		}
	}
}
