package de.tudarmstadt.stg.monto.message;

public class Product {
	private String name;

	public Product(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof Product) {
			Product other = (Product) obj;
			return this.name.equals(other.name);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
