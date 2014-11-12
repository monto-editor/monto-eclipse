package de.tudarmstadt.stg.monto.region;

public class Region implements IRegion {
	private int offset;
	private int length;
	
	public Region(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	public Region(org.eclipse.jface.text.IRegion region) {
		this(region.getOffset(),region.getLength());
	}

	@Override
	public int getStartOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return length;
	}
	
	public String getText(String document) {
		return document.substring(getStartOffset(),getEndOffset());
	}
	
	@Override
	public String toString() {
		return String.format("{offset: %d, length: %d}",offset,length);
	}
}
