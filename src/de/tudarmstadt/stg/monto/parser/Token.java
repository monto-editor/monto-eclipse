package de.tudarmstadt.stg.monto.parser;

public class Token implements AST {
	private int offset;
	private int length;
	
	public Token(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
}
