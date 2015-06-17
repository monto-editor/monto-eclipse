package monto.eclipse.ast;

public interface ASTVisitor {
	public void visit(NonTerminal node);
	public void visit(Terminal token);
}
