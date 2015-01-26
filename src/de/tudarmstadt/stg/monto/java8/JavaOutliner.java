package de.tudarmstadt.stg.monto.java8;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.ui.ISharedImages;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.ast.AST;
import de.tudarmstadt.stg.monto.ast.ASTVisitor;
import de.tudarmstadt.stg.monto.ast.ASTs;
import de.tudarmstadt.stg.monto.ast.NonTerminal;
import de.tudarmstadt.stg.monto.ast.Terminal;
import de.tudarmstadt.stg.monto.connection.AbstractServer;
import de.tudarmstadt.stg.monto.connection.Pair;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.Messages;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.ProductDependency;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;

public class JavaOutliner extends AbstractServer {

	public JavaOutliner(Pair connection) {
		super(connection);
	}

	@Override
	public Either<Exception,ProductMessage> onMessage(List<Message> messages) {
		return Messages.getVersionMessage(messages).flatMap(javaFile ->
		Messages.getProductMessage(messages, Products.ast, Languages.json).flatMap(ast -> {
			try {
				Activator.getProfiler().start(JavaOutliner.class, "onVersionMessage", javaFile);
	
				NonTerminal root = (NonTerminal) ASTs.decode(ast);
				
				OutlineTrimmer trimmer = new OutlineTrimmer();
				root.accept(trimmer);
				Contents content = new StringContent(Outlines.encode(trimmer.getConverted()).toJSONString());
				
				Activator.getProfiler().end(JavaOutliner.class, "onVersionMessage", javaFile);
				
				return Either.right(new ProductMessage(
					javaFile.getVersionId(),
					new LongKey(1),
					javaFile.getSource(), 
					Products.outline, 
					Languages.json,
					content,
					new ProductDependency(ast)));
			} catch (ParseException e) {
				return Either.left(e);
			}
		}));
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
