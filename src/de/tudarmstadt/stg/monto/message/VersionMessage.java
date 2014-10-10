package de.tudarmstadt.stg.monto.message;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VersionMessage {

	private Source source;
	private Contents content;
	private Language language;
	private List<Selection> selections;

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
		version.put("contents", message.getContent().string());
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
}
