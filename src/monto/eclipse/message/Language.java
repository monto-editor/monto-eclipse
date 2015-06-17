package monto.eclipse.message;

public class Language implements Comparable<Language> {
	private String language;

	public Language(String language) {
		this.language = language;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof Language) {
			Language other = (Language) obj;
			return this.language.equals(other.language);
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

	@Override
	public int compareTo(Language other) {
		return this.language.compareTo(other.language);
	}
}
