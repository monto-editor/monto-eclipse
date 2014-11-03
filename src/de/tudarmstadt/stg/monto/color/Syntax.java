package de.tudarmstadt.stg.monto.color;

import de.tudarmstadt.stg.monto.region.Region;


public class Syntax extends Region {

	private Category category;
	
	public Syntax(int offset, int length, Category category) {
		super(offset,length);
		this.category = category;
	}
	
	public Category getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return String.format("(%d,%d,%s)",getStartOffset(),getLength(),category);
	}
}
