package de.tudarmstadt.stg.monto.region;

public interface IRegion {
	public int getStartOffset();
	
	public default int getLength() {
		return getEndOffset() - getStartOffset();
	}

	public default int getEndOffset() {
		return getStartOffset() + getLength();
	}
	
	
	/**
	 * a.inRange(b) tests if a is in range of b.
	 */
	public default boolean inRange(IRegion whole) {
		return this.getStartOffset() >= whole.getStartOffset()
		    && this.getEndOffset() <= whole.getEndOffset();
	}
	
	/**
	 * a.encloses(b) tests if b is in range of a.
	 */
	public default boolean encloses(IRegion part) {
		return part.inRange(this);
	}
}
