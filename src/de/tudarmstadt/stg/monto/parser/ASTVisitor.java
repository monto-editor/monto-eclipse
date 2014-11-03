package de.tudarmstadt.stg.monto.parser;

public interface ASTVisitor {
	public void visit(NonTerminal node);
	public void visit(Terminal token);
}
