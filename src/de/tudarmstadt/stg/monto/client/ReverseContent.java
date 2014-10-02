package de.tudarmstadt.stg.monto.client;

import org.eclipse.imp.language.Language;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;


public class ReverseContent extends AbstractMontoClient {

	private final Product product = new Product("Reversed");
	
	@Override
	public void sendVersionMessage(final Source source, final Language language,
			final Contents contents, final Selection selection) {

		final Contents reversed =
				new StringContent(
				new StringBuilder(
						contents.string()
						).reverse().toString());
		
		listeners.forEach((listener) -> {
			listener.onProductMessage(new ProductMessage(source, product, language, reversed));
		}); 
	}
}
