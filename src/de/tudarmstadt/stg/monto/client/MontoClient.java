package de.tudarmstadt.stg.monto.client;

import java.util.List;
import java.util.stream.Stream;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
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
			List<Selection> selections) {
		return sendVersionMessage(new VersionMessage(source,language,content,selections));
	}	
	public MontoClient sendVersionMessage(VersionMessage msg);
	
	public Stream<Product> availableProducts(Source source, Language language);
	
	public MontoClient addProductMessageListener(ProductMessageListener listener);
	public MontoClient removeProductMessageListener(ProductMessageListener listener);
}
