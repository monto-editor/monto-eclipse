package de.tudarmstadt.stg.monto.server;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class LineSplitter extends AbstractServer {

	private final Product product = new Product("Splitted");

	@Override
	public void onVersionMessage(VersionMessage version) {
		final Contents splitted = new StringContent(version.getContent().toString().replace(' ', '\n'));
		
		emitProductMessage(new ProductMessage(version.getSource(), product, version.getLanguage(), splitted));
	}
}
