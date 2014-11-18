package de.tudarmstadt.stg.monto.completion;

public class Completion {
	
	private String description;
	private String replacement;
	private String icon;
	private int insertionOffset;
	public Completion(String description, String replacement, String icon) {
		this(description,replacement,0,icon);
	}
	public Completion(String description, String replacement, int insertionOffset, String icon) {
		this.description = description;
		this.replacement = replacement;
		this.insertionOffset = insertionOffset;
		this.icon = icon;
	}

	public String getDescription() {
		return description;
	}

	public String getReplacement() {
		return replacement;
	}
	
	public String getIcon() {
		return icon;
	}

	public int getInsertionOffset() {
		return insertionOffset;
	}
}
