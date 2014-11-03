package de.tudarmstadt.stg.monto.outline;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.region.Region;
import de.tudarmstadt.stg.monto.region.Regions;

public class Outlines {

	@SuppressWarnings("unchecked")
	public static JSONObject encode(Outline outline) {
		JSONObject encoding = new JSONObject();
		
		encoding.put("description", outline.getDescription());
		encoding.put("identifier", Regions.encode(outline.getIdentifier()));
		
		if(! outline.isLeaf()) {
			JSONArray childs = new JSONArray();
			outline.getChilds().forEach(child -> childs.add(encode(child)));
			encoding.put("childs", childs);
		}
		
		if(outline.getIcon().isPresent()) {
			encoding.put("icon", outline.getIcon().get());
		}
		
		return encoding;
	}
	

	public static Outline decode(Reader reader) throws ParseException {
		try {
			return decode((JSONObject) JSONValue.parse(reader));
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}
	
	public static Outline decode(JSONObject encoding) throws ParseException {
		try {
			String description = (String) encoding.get("description");
			Region identifier = Regions.decode((JSONObject) encoding.get("identifier"));
			
			String icon = null;
			if(encoding.containsKey("icon")) {
				icon = (String) encoding.get("icon");
			}
			
			List<Outline> childs = new ArrayList<>();
			if(encoding.containsKey("childs")) {
				for(Object child : (JSONArray) encoding.get("childs"))
					childs.add(decode((JSONObject)child));
			}
			
			return new Outline(description, identifier, icon, childs);
			
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}
}
