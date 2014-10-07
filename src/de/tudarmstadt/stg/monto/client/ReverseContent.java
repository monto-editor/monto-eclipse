package de.tudarmstadt.stg.monto.client;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class ReverseContent implements Server {

	private final Product product = new Product("Reversed");

	@Override
	public ProductMessage apply(final VersionMessage version) {
		final Contents reversed =
				new StringContent(
				new StringBuilder(
						version.getContent().string()
						).reverse().toString());
		return new ProductMessage(version.getSource(), product, version.getLanguage(), reversed);
	}

	@Override
	public Product getProduct() {
		return product;
	}
	
}
