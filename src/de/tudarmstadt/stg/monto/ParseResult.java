package de.tudarmstadt.stg.monto;

import de.tudarmstadt.stg.monto.outline.Outline;

public class ParseResult {
	
	private Outline outline;
	private String documentText;

	public ParseResult(Outline outline, String documentText) {
		this.outline = outline;
		this.documentText = documentText;
	}

	public Outline getOutline() {
		return outline;
	}

	public String getDocument() {
		return documentText;
	}
	
}
