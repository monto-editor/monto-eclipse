package de.tudarmstadt.stg.monto.client;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public interface MontoClient extends AutoCloseable {
	public default MontoClient sendVersionMessage(
			Source source,
			Language language,
			Contents content,
			List<Selection> selections) {
		return sendVersionMessage(new VersionMessage(source,language,content,selections));
	}	
	public MontoClient sendVersionMessage(VersionMessage msg);
	
	public Set<Product> availableProducts(Source source);
	
	public MontoClient addProductMessageListener(ProductMessageListener listener);
	public MontoClient removeProductMessageListener(ProductMessageListener listener);
	
	public void connect() throws Exception;
	void listening() throws Exception;

	public default void reconnect() throws Exception {
		close();
		connect();
		listening();
	}
}
