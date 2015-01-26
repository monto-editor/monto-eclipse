package de.tudarmstadt.stg.monto.outline;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.region.Region;
import de.tudarmstadt.stg.monto.region.Regions;

public class Outlines {

	@SuppressWarnings("unchecked")
	public static JSONObject encode(Outline outline) {
		JSONObject encoding = new JSONObject();
		
		encoding.put("description", outline.getDescription());
		encoding.put("identifier", Regions.encode(outline.getIdentifier()));
		
		if(! outline.isLeaf()) {
			JSONArray children = new JSONArray();
			outline.getChildren().forEach(child -> children.add(encode(child)));
			encoding.put("children", children);
		}
		
		if(outline.getIcon().isPresent()) {
			encoding.put("icon", outline.getIcon().get());
		}
		
		return encoding;
	}
	
	public static Outline decode(ProductMessage message) throws ParseException {
		return decode(message.getContents().getReader());
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
			
			List<Outline> children = new ArrayList<>();
			if(encoding.containsKey("children")) {
				for(Object child : (JSONArray) encoding.get("children"))
					children.add(decode((JSONObject)child));
			}
			
			return new Outline(description, identifier, icon, children);
			
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}
}
