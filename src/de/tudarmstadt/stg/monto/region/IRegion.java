package de.tudarmstadt.stg.monto.region;

public interface IRegion {
	public int getStartOffset();
	
	public default int getLength() {
		return getEndOffset() - getStartOffset();
	}

	public default int getEndOffset() {
		return getStartOffset() + getLength();
	}
}
