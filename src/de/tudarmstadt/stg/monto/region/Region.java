package de.tudarmstadt.stg.monto.region;

public class Region implements IRegion {
	private int offset;
	private int length;
	
	public Region(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int getStartOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return length;
	}
	
	public boolean inRange(org.eclipse.jface.text.IRegion region) {
		return getStartOffset() >= region.getOffset()
		    && getStartOffset() + getLength() <= region.getOffset() + region.getLength();
	}
	
	public String getText(String document) {
		return document.substring(getStartOffset(),getEndOffset());
	}
	
	@Override
	public String toString() {
		return String.format("{offset: %d, length: %d}",offset,length);
	}
}
