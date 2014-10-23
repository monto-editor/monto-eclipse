package de.tudarmstadt.stg.monto.client;

import java.util.Optional;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class ReverseContent implements Server {

	private final Product product = new Product("Reversed");

	@Override
	public Optional<ProductMessage> apply(final VersionMessage version) {
		final Contents reversed =
				new StringContent(
				new StringBuilder(
						version.getContent().toString()
						).reverse().toString());
		return Optional.of(new ProductMessage(version.getSource(), product, version.getLanguage(), reversed));
	}
}
