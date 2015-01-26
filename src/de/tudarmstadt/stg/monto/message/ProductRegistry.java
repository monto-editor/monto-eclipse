package de.tudarmstadt.stg.monto.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductRegistry {

	private Map<Source,Set<ProductItem>> registeredProducts = new HashMap<>();

	public void registerProduct(final Source source, final Product product, final Language language) {
		registeredProducts.compute(source, (_source,products) -> {
			if(products == null)
				products = new HashSet<>();
			products.add(new ProductItem(product,language));
			return products;
		});
	}

	public Set<ProductItem> availableProducts(Source source) {
		return registeredProducts.getOrDefault(source, new HashSet<>());
	}
	
	public static class ProductItem {
		private Product product;
		private Language language;

		public ProductItem(Product product, Language language) {
			this.product = product;
			this.language = language;
		}

		public Product getProduct() {
			return product;
		}

		public Language getLanguage() {
			return language;
		}
		
		@Override
		public int hashCode() {
			return (product.toString() + language.toString()).hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof ProductItem) {
				ProductItem other = (ProductItem) obj;
				return this.product.equals(other.product) && this.language.equals(other.language);
			} else {
				return false;
			}
		}
	}
}
