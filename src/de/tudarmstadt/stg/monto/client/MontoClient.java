package de.tudarmstadt.stg.monto.client;

import java.util.stream.Stream;

import org.eclipse.imp.language.Language;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public interface MontoClient {
	public default MontoClient sendVersionMessage(
			Source source,
			Language language,
			Contents content,
			Selection selection) {
		return sendVersionMessage(new VersionMessage(source,language,content,selection));
	}	
	public MontoClient sendVersionMessage(VersionMessage msg);
	
	public Stream<Product> availableProducts(Source source, Language language);
	
	public MontoClient addProductMessageListener(ProductMessageListener listener);
	public MontoClient removeProductMessageListener(ProductMessageListener listener);
}
