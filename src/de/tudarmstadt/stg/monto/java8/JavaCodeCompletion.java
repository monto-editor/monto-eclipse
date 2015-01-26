package de.tudarmstadt.stg.monto.java8;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.ui.ISharedImages;

import de.tudarmstadt.stg.monto.Activator;
import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.ast.AST;
import de.tudarmstadt.stg.monto.ast.ASTVisitor;
import de.tudarmstadt.stg.monto.ast.ASTs;
import de.tudarmstadt.stg.monto.ast.NonTerminal;
import de.tudarmstadt.stg.monto.ast.Terminal;
import de.tudarmstadt.stg.monto.completion.Completion;
import de.tudarmstadt.stg.monto.completion.Completions;
import de.tudarmstadt.stg.monto.connection.AbstractServer;
import de.tudarmstadt.stg.monto.connection.Pair;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.Messages;
import de.tudarmstadt.stg.monto.message.ProductDependency;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.region.IRegion;

public class JavaCodeCompletion extends AbstractServer {

	public JavaCodeCompletion(Pair connection) {
		super(connection);
	}

	@Override
	public Either<Exception,ProductMessage> onMessage(List<Message> messages) {
		return Messages.getVersionMessage(messages).flatMap(javaFile ->
		Messages.getProductMessage(messages, Products.ast, Languages.json).flatMap(ast -> {
			
			if(javaFile.getSelections().size() > 0) {
				try {
					Activator.getProfiler().start(JavaCodeCompletion.class, "onVersionMessage", javaFile);
					
					AST root = ASTs.decode(ast);
					List<Completion> allcompletions = allCompletions(javaFile.getContent(),root);
					List<AST> selectedPath = selectedPath(root, javaFile.getSelections().get(0));
					
					if(selectedPath.size() > 0 && last(selectedPath) instanceof Terminal) {
						Terminal terminalToBeCompleted = (Terminal) last(selectedPath);
						String toBeCompleted = javaFile.getContent().extract(terminalToBeCompleted).toString();
						Stream<Completion> relevant = 
								allcompletions
								.stream()
								.filter(comp -> comp.getReplacement().startsWith(toBeCompleted))
								.map(comp -> new Completion(
										comp.getDescription() + ": " + comp.getReplacement(),
										comp.getReplacement().substring(toBeCompleted.length()),
										javaFile.getSelections().get(0).getStartOffset(),
										comp.getIcon()));
						
						Contents content = new StringContent(Completions.encode(relevant).toJSONString());
						
						Activator.getProfiler().end(JavaCodeCompletion.class, "onVersionMessage", javaFile);
						
						return Either.right(new ProductMessage(
							javaFile.getVersionId(),
							new LongKey(1),
							javaFile.getSource(),
							Products.completions,
							Languages.json,
							content,
							new ProductDependency(ast)));
					}
				} catch (Exception e) {
					return Either.left(e);
				}	
			}
			return Either.left(new IllegalArgumentException("Code completion needs selection"));
		}));
	}
	
	private static List<Completion> allCompletions(Contents contents, AST root) {
		AllCompletions completionVisitor = new AllCompletions(contents);
		root.accept(completionVisitor);
		return completionVisitor.getCompletions();
	}
	
	private static class AllCompletions implements ASTVisitor {

		private List<Completion> completions = new ArrayList<>();
		private Contents content;
		private boolean fieldDeclaration;
		
		public AllCompletions(Contents content) {
			this.content = content;
		}
		
		@Override
		public void visit(NonTerminal node) {
			switch(node.getName()) {
				
				case "packageDeclaration":
					AST packageIdentifier = node.getChildren().get(1);
					completions.add(new Completion(
							"package",
							content.extract(packageIdentifier).toString(),
							ISharedImages.IMG_OBJS_PACKDECL));
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
			Terminal structureIdent = (Terminal) node
					.getChildren()
					.stream()
					.filter(ast -> ast instanceof Terminal)
					.reduce((previous,current) -> current).get();
			completions.add(new Completion(name,content.extract(structureIdent).toString(),icon));
			node.getChildren().forEach(child -> child.accept(this));
		}
		
		private void leaf(NonTerminal node, String name, String icon) {
			AST ident = node
					.getChildren()
					.stream()
					.filter(ast -> ast instanceof Terminal)
					.findFirst().get();
			completions.add(new Completion(name, content.extract(ident).toString(), icon));
		}
		

		public List<Completion> getCompletions() {
			return completions;
		}
	}

	private static List<AST> selectedPath(AST root, Selection sel) {
		SelectedPath finder = new SelectedPath(sel);
		root.accept(finder);
		return finder.getSelected();
	}
	
	private static class SelectedPath implements ASTVisitor	{
		
		private Selection selection;
		private List<AST> selectedPath = new ArrayList<>();

		public SelectedPath(Selection selection) {
			this.selection = selection;
		}
		
		@Override
		public void visit(NonTerminal node) {
			if(selection.inRange(node) || rightBehind(selection,node))
				selectedPath.add(node);
			node.getChildren()
				.stream()
				.filter(child -> selection.inRange(child) || rightBehind(selection,child))
				.forEach(child -> child.accept(this));
		}

		@Override
		public void visit(Terminal token) {
			if(rightBehind(selection,token))
				selectedPath.add(token);
		}
		
		public List<AST> getSelected() {
			return selectedPath;
		}
		
		private static boolean rightBehind(IRegion region1, IRegion region2) {
			try {
				return region1.getStartOffset() == region2.getEndOffset();
			} catch(Exception e) {
				return false;
			}
		}
	}

	private static <A> A last(List<A> list) {
		return list.get(list.size()-1);
	}
}
