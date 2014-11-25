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
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;
import de.tudarmstadt.stg.monto.server.ProductMessageListener;
import de.tudarmstadt.stg.monto.server.StatefullServer;

public class JavaOutliner extends StatefullServer implements ProductMessageListener {

	@Override
	protected boolean isRelevant(VersionMessage message) {
		return message.getLanguage().equals(Languages.java);
	}
	
	public void receiveVersionMessage(VersionMessage message) {}

	@Override
	public void onProductMessage(ProductMessage message) {
		VersionMessage latest = getLatestVersionMessage(message.getSource());
		if(message.getLanguage().equals(Languages.json)
	    && message.getProduct().equals(Products.ast) && latest != null && message.getId().equals(latest.getId())) {
			
			
			try {
				Activator.getProfiler().start(JavaOutliner.class, "onVersionMessage", message);

				NonTerminal root = (NonTerminal) ASTs.decode(message);
				
				OutlineTrimmer trimmer = new OutlineTrimmer();
				root.accept(trimmer);
				Contents content = new StringContent(Outlines.encode(trimmer.getConverted()).toJSONString());
				
				Activator.getProfiler().end(JavaOutliner.class, "onVersionMessage", message);
				
				emitProductMessage(
						new ProductMessage(
								message.getId(),
								message.getSource(), 
								Products.outline, 
								Languages.json,
								content));
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
					node.getChildren().forEach(child -> child.accept(this));
					// compilation unit doesn't get poped from the stack
					// to be available as a return value.
					break;
				
				case "packageDeclaration":
					AST packageIdentifier = node.getChildren().get(1);
					if(packageIdentifier instanceof Terminal)
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
					node.getChildren().forEach(child -> child.accept(this));
					fieldDeclaration = false;
				
				case "variableDeclaratorId":
					if(fieldDeclaration)
						leaf(node, "field", ISharedImages.IMG_OBJS_PRIVATE);
					break;
				
				case "methodDeclarator":
					leaf(node, "method", ISharedImages.IMG_OBJS_PUBLIC);
					
				default:
					node.getChildren().forEach(child -> child.accept(this));
			}
		}

		@Override
		public void visit(Terminal token) {
			
		}
		
		private void structureDeclaration(NonTerminal node, String name, String icon) {
			node.getChildren()
				.stream()
				.filter(ast -> ast instanceof Terminal)
				.limit(2)
				.reduce((previous,current) -> current)
				.ifPresent(ident -> {		
					Outline structure = new Outline(name,ident,icon);
					converted.peek().addChild(structure);
					converted.push(structure);
					node.getChildren().forEach(child -> child.accept(this));
					converted.pop();
				});
		}
		
		private void leaf(NonTerminal node, String name, String icon) {
			node.getChildren()
				.stream()
				.filter(ast -> ast instanceof Terminal)
				.findFirst()
				.ifPresent(ident -> {						
					converted.peek().addChild(new Outline(name, ident, icon));
				});
		}
	}

}
