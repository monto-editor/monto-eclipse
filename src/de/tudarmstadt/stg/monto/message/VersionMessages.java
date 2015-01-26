package de.tudarmstadt.stg.monto.message;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.Activator;

public class VersionMessages {

	public static VersionMessage decode(final Reader reader) throws ParseException {
		final JSONObject message = (JSONObject) JSONValue.parse(reader);
		return decode(message);
	}	
	
	@SuppressWarnings("unchecked")
	public static VersionMessage decode(JSONObject message) throws ParseException {
		try {
			final long start = System.nanoTime();
			final LongKey id = new LongKey((Long) message.get("version_id"));
			final Source source = new Source((String) message.get("source"));
			final Language language = new Language((String) message.get("language"));
			final Contents contents = new StringContent((String) message.get("contents"));
			final JSONArray selectionsArray = (JSONArray) message.getOrDefault("selections", new JSONArray());
			final List<Selection> selections = new ArrayList<>(selectionsArray.size());
			Iterator<JSONObject> iterator = selectionsArray.iterator();
			while(iterator.hasNext()) {
				final JSONObject selection = iterator.next();
				final Long begin = (Long) selection.get("begin");
				final Long end = (Long) selection.get("end");
				selections.add(new Selection(begin.intValue(), end.intValue() - begin.intValue()));
			}
			JSONArray invalid = (JSONArray) message.getOrDefault("invalid", new JSONArray());
			List<Dependency> invalidProducts = Dependencies.decode(invalid);
			final VersionMessage msg = new VersionMessage(id, source, language, contents, selections, invalidProducts);
			Activator.getProfiler().start(VersionMessage.class, "decode", msg, start);
			Activator.getProfiler().end(VersionMessage.class, "decode", msg);
			return msg;
		} catch (Exception e) {
			throw new ParseException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encode(VersionMessage message) {
		Activator.getProfiler().start(VersionMessage.class, "encode", message);
		JSONObject version = new JSONObject();
		version.put("version_id", message.getVersionId().longValue());
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
		JSONArray invalidProducts = Dependencies.encode(message.getInvalid());
		version.put("invalid",invalidProducts);
		Activator.getProfiler().end(VersionMessage.class, "encode", message);
		return version;
	}

}
