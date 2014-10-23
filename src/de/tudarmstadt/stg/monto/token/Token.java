package de.tudarmstadt.stg.monto.token;

import org.eclipse.jface.text.IRegion;

public class Token {

	private int offset;
	private int length;
	private Category category;
	
	public Token(int offset, int length, Category category) {
		this.offset = offset;
		this.length = length;
		this.category = category;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getOffset() {
		return offset;
	}

	public Category getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return String.format("(%d,%d,%s)",offset,length,category);
	}
	
	
	public boolean inRange(IRegion region) {
		return getOffset() >= region.getOffset()
		    && getOffset() + getLength() < region.getOffset() + region.getLength();
	}

}
