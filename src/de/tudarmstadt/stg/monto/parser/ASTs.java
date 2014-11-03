package de.tudarmstadt.stg.monto.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.region.Region;
import de.tudarmstadt.stg.monto.region.Regions;


/**
 * The JSON AST format is inspired by this mailing list entry:
 * {@link https://mail.mozilla.org/pipermail/es-discuss/2009-December/010228.html}
 * 
 * The format uses nested arrays to represent nodes and branches in the ast.
 * The AST with non-terminals A and B and terminals s, t and u
 * 
 * <code>
 *    A
 *   / \
 *  s   B
 *     / \
 *    t   u
 * </code>
 * 
 * becomes the following JSON:
 * 
 * <code>
 * ["A",
 *   {offset:..., length:...},    // terminal s
 *   ["B",
 *      {offset:..., length:...}, // terminal t
 *      {offset:..., length:...}  // terminal u
 *   ]
 * ]
 * </code>
 */
public class ASTs {
	
	public static Contents encode(AST ast) {
		Encoder encoder = new Encoder();
		ast.accept(encoder);
		if(encoder.getEncoding() instanceof JSONObject) {
			return new StringContent(((JSONObject) encoder.getEncoding()).toJSONString());
		} else if (encoder.getEncoding() instanceof JSONArray) {
			return new StringContent(((JSONArray) encoder.getEncoding()).toJSONString());
		} else {
			return null;
		}	
	}
	
	private static class Encoder implements ASTVisitor {
		
		private Object encoding;
		
		public Object getEncoding() {
			return encoding;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void visit(NonTerminal node) {
			JSONArray jsonNode = new JSONArray();
			jsonNode.add(node.getName());
			for(AST child : node.getChilds()) {
				child.accept(this);
				jsonNode.add(encoding);
			}
			encoding = jsonNode;
		}

		@Override
		public void visit(Terminal token) {
			encoding = Regions.encode(token);
		}
	}
	
	public static AST decode(ProductMessage message) throws ParseException {
		try {
			Object json = JSONValue.parse(message.getContents().getReader());
			return decode(json);
		} catch(Exception e) {
			throw new ParseException(e);
		}
	}
	
	private static AST decode(Object json) throws ParseException {
		if(json instanceof JSONObject) {
			return decode((JSONObject) json);
		} else if (json instanceof JSONArray) {
			return decode((JSONArray) json);
		} else {
			return null;
		}
	}
	
	private static AST decode(JSONObject encoding) throws ParseException {
		Region region = Regions.decode(encoding);
		return new Terminal(region.getStartOffset(),region.getLength());
	}
	
	private static AST decode(JSONArray jsonArray) throws ParseException {
		String name = (String) jsonArray.remove(0);
		List<AST> childs = new ArrayList<>(jsonArray.size());
		
		for(Object object : jsonArray)
			childs.add(decode(object));
		
		return new NonTerminal(name,childs);
	}
}
