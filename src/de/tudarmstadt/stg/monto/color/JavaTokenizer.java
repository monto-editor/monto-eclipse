package de.tudarmstadt.stg.monto.color;

import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.server.AbstractServer;
import de.tudarmstadt.stg.monto.token.java8.Java8Lexer;

public class JavaTokenizer extends AbstractServer {
	
	private static final Product product = new Product("tokens");
	
	@Override
	public void onVersionMessage(VersionMessage msg) {
		if(msg.getLanguage().toString().equals("java")) { 
			try {
				Java8Lexer lexer = new Java8Lexer(new ANTLRInputStream(msg.getContent().getReader()));
				List<Syntax> tokens = lexer.getAllTokens().stream().map(token -> convertToken(token)).collect(Collectors.toList());
				emitProductMessage(
						new ProductMessage(
								msg.getSource(),
								product,
								msg.getLanguage(),
								new StringContent(Syntaxes.encode(tokens).toJSONString())));
				
			} catch (Exception e) {
				
			}
		}
	}

	private Syntax convertToken(Token token) {
		int offset = token.getStartIndex();
		int length = token.getStopIndex() - offset + 1;
		
		Category category;
		switch(token.getType()) {
			
			case Java8Lexer.COMMENT:
			case Java8Lexer.LINE_COMMENT:
				category = Category.COMMENT;
				break;
			
			case Java8Lexer.CONST:
			case Java8Lexer.NullLiteral:
				category = Category.CONSTANT;
				break;
						
			case Java8Lexer.StringLiteral:
				category = Category.STRING;
				break;
						
			case Java8Lexer.CharacterLiteral:
				category = Category.CHARACTER;
				break;
				
			case Java8Lexer.IntegerLiteral:
				category = Category.NUMBER;
				break;
				
			case Java8Lexer.BooleanLiteral:
				category = Category.BOOLEAN;
				break;
				
			case Java8Lexer.FloatingPointLiteral:
				category = Category.FLOAT;
				break;
			
			case Java8Lexer.Identifier:
				category = Category.IDENTIFIER;
				break;

			case Java8Lexer.IF:
			case Java8Lexer.ELSE:
			case Java8Lexer.SWITCH:
				category = Category.CONDITIONAL;
				break;

			case Java8Lexer.FOR:
			case Java8Lexer.DO:
			case Java8Lexer.WHILE:
			case Java8Lexer.CONTINUE:
			case Java8Lexer.BREAK:
				category = Category.REPEAT;
				break;
			
			case Java8Lexer.CASE:
			case Java8Lexer.DEFAULT:
				category = Category.LABEL;
				break;
			
			case Java8Lexer.ADD:
			case Java8Lexer.ADD_ASSIGN:
			case Java8Lexer.SUB:
			case Java8Lexer.SUB_ASSIGN:
			case Java8Lexer.MUL:
			case Java8Lexer.MUL_ASSIGN:
			case Java8Lexer.DIV:
			case Java8Lexer.DIV_ASSIGN:
			case Java8Lexer.MOD:
			case Java8Lexer.MOD_ASSIGN:
			case Java8Lexer.INC:
			case Java8Lexer.DEC:
			case Java8Lexer.AND:
			case Java8Lexer.AND_ASSIGN:
			case Java8Lexer.BITAND:
			case Java8Lexer.OR:
			case Java8Lexer.OR_ASSIGN:
			case Java8Lexer.BITOR:
			case Java8Lexer.CARET:
			case Java8Lexer.XOR_ASSIGN:
			case Java8Lexer.GT:
			case Java8Lexer.GE:
			case Java8Lexer.LT:
			case Java8Lexer.LE:
			case Java8Lexer.TILDE:
			case Java8Lexer.LSHIFT_ASSIGN:
			case Java8Lexer.RSHIFT_ASSIGN:
			case Java8Lexer.URSHIFT_ASSIGN:
			case Java8Lexer.ASSIGN:
			case Java8Lexer.BANG:
			case Java8Lexer.EQUAL:
			case Java8Lexer.NOTEQUAL:
			case Java8Lexer.QUESTION:
			case Java8Lexer.COLON:
			case Java8Lexer.INSTANCEOF:
				category = Category.OPERATOR;
				break;

			case Java8Lexer.TRY:
			case Java8Lexer.CATCH:
			case Java8Lexer.THROW:
			case Java8Lexer.THROWS:
			case Java8Lexer.FINALLY:
				category = Category.EXCEPTION;
				break;
				
			case Java8Lexer.BOOLEAN:
			case Java8Lexer.BYTE:
			case Java8Lexer.CHAR:
			case Java8Lexer.DOUBLE:
			case Java8Lexer.FLOAT:
			case Java8Lexer.INT:
			case Java8Lexer.LONG:
			case Java8Lexer.SHORT:
			case Java8Lexer.VOID:
				category = Category.TYPE;
				break;

			case Java8Lexer.ABSTRACT:
			case Java8Lexer.PRIVATE:
			case Java8Lexer.PROTECTED:
			case Java8Lexer.PUBLIC:
			case Java8Lexer.STATIC:
			case Java8Lexer.SYNCHRONIZED:
			case Java8Lexer.VOLATILE:
			case Java8Lexer.FINAL:
			case Java8Lexer.TRANSIENT:
			case Java8Lexer.NATIVE:
			case Java8Lexer.STRICTFP:
				category = Category.MODIFIER;
				break;

			case Java8Lexer.CLASS:
			case Java8Lexer.ENUM:
			case Java8Lexer.INTERFACE:
				category = Category.STRUCTURE;
				break;

			case Java8Lexer.EXTENDS:
			case Java8Lexer.IMPLEMENTS:
			case Java8Lexer.IMPORT:
			case Java8Lexer.PACKAGE:
			case Java8Lexer.THIS:
			case Java8Lexer.SUPER:
			case Java8Lexer.GOTO:
			case Java8Lexer.NEW:
			case Java8Lexer.RETURN:
			case Java8Lexer.ASSERT:
				category = Category.KEYWORD;
				break;
			
			case Java8Lexer.LPAREN:
			case Java8Lexer.RPAREN:
			case Java8Lexer.LBRACE:
			case Java8Lexer.RBRACE:
			case Java8Lexer.LBRACK:
			case Java8Lexer.RBRACK:
				category = Category.PARENTHESIS;
				break;
				
			case Java8Lexer.COMMA:
			case Java8Lexer.SEMI:
			case Java8Lexer.DOT:
			case Java8Lexer.ELLIPSIS:
			case Java8Lexer.ARROW:
			case Java8Lexer.COLONCOLON:
				category = Category.DELIMITER;
				break;				
			
			case Java8Lexer.AT:
				category = Category.META;
				break;
				
			case Java8Lexer.WS:
				category = Category.WHITESPACE;
				break;
			
			default:
				category = Category.UNKNOWN;
		}
		
		return new Syntax(offset,length,category);
	}
}
