package monto.eclipse.message;

public class Product implements Comparable<Product> {
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

	@Override
	public int compareTo(Product other) {
		return this.name.compareTo(other.name);
	}
}
