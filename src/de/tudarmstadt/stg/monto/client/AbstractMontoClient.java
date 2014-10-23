package de.tudarmstadt.stg.monto.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Source;

public abstract class AbstractMontoClient implements MontoClient {

	protected List<ProductMessageListener> listeners = new ArrayList<>();
	protected Map<Source,Set<Product>> registeredProducts = new HashMap<>();

	@Override
	public MontoClient addProductMessageListener(ProductMessageListener listener) {
		listeners.add(listener);
		return this;
	}

	@Override
	public MontoClient removeProductMessageListener(ProductMessageListener listener) {
		listeners.remove(listener);
		return this;
	}
	
	protected void registerProduct(final Source source, final Product product) {
		registeredProducts.compute(source, (_source,products) -> {
			if(products == null)
				products = new HashSet<>();
			products.add(product);
			return products;
		});
	}

	@Override
	public Set<Product> availableProducts(Source source) {
		return registeredProducts.getOrDefault(source, new HashSet<>());
	}
}
