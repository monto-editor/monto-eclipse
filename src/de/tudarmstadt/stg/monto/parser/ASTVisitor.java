package de.tudarmstadt.stg.monto.parser;

public interface ASTVisitor {
	public void visit(Node node);
	public void visit(Token token);
}
