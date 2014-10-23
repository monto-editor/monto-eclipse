package de.tudarmstadt.stg.monto.client;

import java.util.Optional;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class LineSplitter implements Server {

	private final Product product = new Product("Splitted");

	@Override
	public Optional<ProductMessage> apply(final VersionMessage version) {
		final Contents splitted = new StringContent(version.getContent().toString().replace(' ', '\n'));
		
		return Optional.of(new ProductMessage(version.getSource(), product, version.getLanguage(), splitted));
	}
}
