package de.tudarmstadt.stg.monto.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductRegistry {

	private Map<Source,Set<Product>> registeredProducts = new HashMap<>();
	
	public void registerProduct(final Source source, final Product product) {
		registeredProducts.compute(source, (_source,products) -> {
			if(products == null)
				products = new HashSet<>();
			products.add(product);
			return products;
		});
	}

	public Set<Product> availableProducts(Source source) {
		return registeredProducts.getOrDefault(source, new HashSet<>());
	}
}
