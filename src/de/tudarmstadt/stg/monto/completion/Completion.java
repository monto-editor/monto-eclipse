package de.tudarmstadt.stg.monto.completion;

public class Completion {
	
	private String description;
	private String replacement;
	private String icon;
	
	public Completion(String description, String replacement, String icon) {
		this.description = description;
		this.replacement = replacement;
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
}
