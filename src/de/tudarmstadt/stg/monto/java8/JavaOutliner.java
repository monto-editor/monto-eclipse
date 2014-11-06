package de.tudarmstadt.stg.monto.java8;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.ui.ISharedImages;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.ast.AST;
import de.tudarmstadt.stg.monto.ast.ASTVisitor;
import de.tudarmstadt.stg.monto.ast.ASTs;
import de.tudarmstadt.stg.monto.ast.NonTerminal;
import de.tudarmstadt.stg.monto.ast.Terminal;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;
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
				
				// cannot create new outline if the AST is incomplete
				if(!JavaParser.isComplete(root))
					return;
				
				OutlineTrimmer trimmer = new OutlineTrimmer();
				root.accept(trimmer);
				
				emitProductMessage(
						new ProductMessage(
								message.getSource(), 
								outline, 
								message.getLanguage(),
								new StringContent(Outlines.encode(trimmer.getConverted()).toJSONString())
								));
			} catch (ParseException e) {
				Activator.error(e);
			}
		}
	}
	
	/**
	 * Traverses the AST and removes unneeded information.
	 */
	private static class OutlineTrimmer implements ASTVisitor {

		private Deque<Outline> converted = new ArrayDeque<>();
		private boolean fieldDeclaration = false;
		
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
				
				case "fieldDeclaration":
					fieldDeclaration = true;
					node.getChilds().forEach(child -> child.accept(this));
					fieldDeclaration = false;
				
				case "variableDeclaratorId":
					if(fieldDeclaration)
						leaf(node, "field", ISharedImages.IMG_OBJS_PRIVATE);
					break;
				
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
