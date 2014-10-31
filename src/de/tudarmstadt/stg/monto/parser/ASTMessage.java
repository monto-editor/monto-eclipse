package de.tudarmstadt.stg.monto.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;


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
public class ASTMessage {
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
		public void visit(Node node) {
			JSONArray jsonNode = new JSONArray();
			jsonNode.add(node.getName());
			for(AST child : node.getChilds()) {
				child.accept(this);
				jsonNode.add(encoding);
			}
			encoding = jsonNode;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void visit(Token token) {
			JSONObject jsonToken = new JSONObject();
			jsonToken.put("offset", token.getOffset());
			jsonToken.put("length", token.getLength());
			encoding = jsonToken;
		}
	}
	
	public AST decode(ProductMessage message) throws ASTMessageParseException {
		try {
			Object json = JSONValue.parse(message.getContents().getReader());
			return decode(json);
		} catch(Exception e) {
			throw new ASTMessageParseException(e);
		}
	}
	
	private AST decode(Object json) {
		if(json instanceof JSONObject) {
			return decode((JSONObject) json);
		} else if (json instanceof JSONArray) {
			return decode((JSONArray) json);
		} else {
			return null;
		}
	}
	
	private AST decode(JSONObject jsonObject) {
		Long offset = (Long) jsonObject.get("offset");
		Long length = (Long) jsonObject.get("length");
		
		return new Token(offset.intValue(), length.intValue());
	}
	
	private AST decode(JSONArray jsonArray) {
		String name = (String) jsonArray.remove(0);
		List<AST> childs = new ArrayList<>(jsonArray.size());
		
		for(Object object : jsonArray)
			childs.add(decode(object));
		
		return new Node(name,childs);
	}
}
