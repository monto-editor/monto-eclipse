package de.tudarmstadt.stg.monto.client;

import org.eclipse.imp.language.Language;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;

public class LineSplitter extends AbstractMontoClient {

	private Product product = new Product("Splitted");

	@Override
	public void sendVersionMessage(Source source, Language language,
			Contents contents, Selection selection) {
		
		final Contents splitted = new StringContent(contents.string().replace(' ', '\n'));
		
		listeners.forEach((listener) -> {
			listener.onProductMessage(new ProductMessage(source, product , language, splitted));
		}); 
	}
}
