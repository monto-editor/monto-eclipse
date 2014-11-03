package de.tudarmstadt.stg.monto.outline;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.ui.ISharedImages;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.parser.AST;
import de.tudarmstadt.stg.monto.parser.ASTParseException;
import de.tudarmstadt.stg.monto.parser.ASTVisitor;
import de.tudarmstadt.stg.monto.parser.ASTs;
import de.tudarmstadt.stg.monto.parser.NonTerminal;
import de.tudarmstadt.stg.monto.parser.Terminal;
import de.tudarmstadt.stg.monto.server.AbstractServer;
import de.tudarmstadt.stg.monto.server.ProductMessageListener;

public class JavaOutliner extends AbstractServer implements ProductMessageListener {
	
	public static final Product outline = new Product("outline");
	
	public void onVersionMessage(VersionMessage message) {
	}

	@Override
	public void onProductMessage(ProductMessage message) {
		if(isJavaAst(message)) {
			try {
				NonTerminal root = (NonTerminal) ASTs.decode(message);		
				OutlineConverter converter = new OutlineConverter();
				root.accept(converter);
				
				emitProductMessage(
						new ProductMessage(
								message.getSource(), 
								outline, 
								message.getLanguage(),
								new StringContent(Outlines.encode(converter.getConverted()).toJSONString())
								));
			} catch (ASTParseException e) {
				Activator.error(e);
			}
		}
	}
	
	private static class OutlineConverter implements ASTVisitor {

		private Deque<Outline> converted = new ArrayDeque<>();
		
		public Outline getConverted() {
			return converted.getFirst();
		}
		
		@Override
		public void visit(NonTerminal node) {
			switch(node.getName()) {
				case "compilationUnit":
					converted.push(new Outline("compilationUnit", node, null));
					node.getChilds().forEach(child -> child.accept(this));
					// compilation unit doesn't get poped from the stack
					// to be available as a return value.
					break;
				
				case "packageDeclaration":
					AST packageIdentifier = node.getChilds().get(1);
					converted.peek().addChild(new Outline("package",packageIdentifier,ISharedImages.IMG_OBJS_PACKDECL));
					break;
					
				case "normalClassDeclaration":
					structureDeclaration(node, "class", ISharedImages.IMG_OBJS_CLASS);
					break;
					
				case "enumDeclaration":
					structureDeclaration(node, "enum", ISharedImages.IMG_OBJS_ENUM);
					break;
					
				case "enumConstant":
					leaf(node, "constant", ISharedImages.IMG_OBJS_ENUM_DEFAULT);
					break;
					
//				case "variableDeclaratorId":
//					onIdentifier(node, "field");
//					break;
				
				case "methodDeclarator":
					leaf(node, "method", ISharedImages.IMG_OBJS_PUBLIC);
					
				default:
					node.getChilds().forEach(child -> child.accept(this));
			}
		}

		@Override
		public void visit(Terminal token) {
			
		}
		
		private void structureDeclaration(NonTerminal node, String name, String icon) {
			Terminal structureIdent = (Terminal) node
					.getChilds()
					.stream()
					.filter(ast -> ast instanceof Terminal)
					.reduce((previous,current) -> current).get();
			Outline structure = new Outline(name,structureIdent,icon);
			converted.peek().addChild(structure);
			converted.push(structure);
			node.getChilds().forEach(child -> child.accept(this));
			converted.pop();
		}
		
		private void leaf(NonTerminal node, String name, String icon) {
			AST ident = node
					.getChilds()
					.stream()
					.filter(ast -> ast instanceof Terminal)
					.findFirst().get();
			converted.peek().addChild(new Outline(name, ident, icon));
		}
	}
	
	private static boolean isJavaAst(ProductMessage message) {
		return isJava(message.getLanguage()) && isAST(message.getProduct());
	}
	
	private static boolean isJava(Language language) {
		return language.toString().equals("java");
	}
	
	private static boolean isAST(Product product) {
		return product.toString().equals("ast");
	}
}
