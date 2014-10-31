package de.tudarmstadt.stg.monto.parser;

public interface AST {
	public void accept(ASTVisitor visitor);
}
