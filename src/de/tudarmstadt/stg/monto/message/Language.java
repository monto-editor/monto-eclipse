package de.tudarmstadt.stg.monto.message;

public class Language {
	private String language;

	public Language(String language) {
		this.language = language;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof Language) {
			Language other = (Language) obj;
			return this.language.equals(other);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return language;
	}
	
	@Override
	public int hashCode() {
		return language.hashCode();
	}
}
