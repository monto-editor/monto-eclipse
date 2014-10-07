package de.tudarmstadt.stg.monto.client;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class LineSplitter implements Server {

	private final Product product = new Product("Splitted");

	@Override
	public ProductMessage apply(final VersionMessage version) {
		final Contents splitted = new StringContent(version.getContent().string().replace(' ', '\n'));
		
		return new ProductMessage(version.getSource(), product, version.getLanguage(), splitted);
	}

	@Override
	public Product getProduct() {
		return product;
	}
}
