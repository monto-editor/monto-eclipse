package de.tudarmstadt.stg.monto.token;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessageParseException;
import de.tudarmstadt.stg.monto.message.StringContent;

/**
 * Encodes and decodes messages that contain the result of an arbitrary tokenization.
 * 
 * The message contents must have the following format:
 * {@code [{offset:<int>,length:<int>,category:<string>}]}
 * where offset represents the offset of the token in the document,
 * length represents the length of the text of the token and
 * category represents the type of token like keyword or identifier.
 */
public class TokenMessage {
	
	/**
	 * Encodes a list of tokens in the appropriate message format.
	 */
	@SuppressWarnings("unchecked")
	public static Contents encode(final List<Token> tokens) {
		final JSONArray tokenArray = new JSONArray();
		
		for(Token token : tokens) {
			JSONObject jsonToken = new JSONObject();
			jsonToken.put("offset", token.getOffset());
			jsonToken.put("length", token.getLength());
			jsonToken.put("category", token.getCategory().toString().toLowerCase());
			tokenArray.add(jsonToken);
		}
		return new StringContent(tokenArray.toString());
	}
	
	/**
	 * Decodes a product message that contains a tokenization result
	 * into a list of tokens.
	 */
	public static List<Token> decode(ProductMessage message) throws ProductMessageParseException {
		try {
			JSONArray content = (JSONArray) JSONValue.parse(message.getContents().getReader());
			
			List<Token> tokens = new ArrayList<>();
			for(Object tokenObj : content) {
				JSONObject token = (JSONObject) tokenObj;
				Long offset = (Long) token.get("offset");
				Long length = (Long) token.get("length");
				String category = (String) token.get("category");
				tokens.add(new Token(offset.intValue(),length.intValue(),Category.fromString(category.toUpperCase())));
			}
			return tokens;
		} catch(Exception e) {
			throw new ProductMessageParseException(e);
		}
	}

}
