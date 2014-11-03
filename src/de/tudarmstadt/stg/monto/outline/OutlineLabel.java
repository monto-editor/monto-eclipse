package de.tudarmstadt.stg.monto.outline;

import de.tudarmstadt.stg.monto.region.IRegion;

public class OutlineLabel implements IRegion {
	private Outline outline;
	private String document;
	
	public OutlineLabel(Outline outline, String document) {
		this.outline = outline;
		this.document = document;
	}

	public Outline getOutline() {
		return outline;
	}

	public String getText() {
		return document.substring(outline.getStartOffset(), outline.getEndOffset());
	}

	@Override
	public int getStartOffset() {
		return outline.getStartOffset();
	}
	
	@Override
	public int getLength() {
		return outline.getLength();
	}
	
}
