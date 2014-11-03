package de.tudarmstadt.stg.monto.region;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.ParseException;

public class Regions {

	@SuppressWarnings("unchecked")
	public static JSONObject encode(IRegion range) {
		JSONObject encoding = new JSONObject();
		encoding.put("offset", range.getStartOffset());
		encoding.put("length", range.getLength());
		return encoding;
	}

	public static Region decode(Reader rangeEncoding) throws ParseException {
		try {
			return decode((JSONObject) JSONValue.parse(rangeEncoding));
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}

	public static Region decode(JSONObject encoding) throws ParseException {
		try {
			Long offset = (Long) encoding.get("offset");
			Long length = (Long) encoding.get("length");
			return new Region(offset.intValue(),length.intValue());
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}
}
