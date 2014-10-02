package de.tudarmstadt.stg.monto.client;

import org.eclipse.imp.language.Language;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;

public interface MontoClient {
	public void sendVersionMessage(
			Source source,
			Language language,
			Contents contents,
			Selection selection
			);
	
	public void addProductMessageListener(ProductMessageListener listener);
	public void removeProductMessageListener(ProductMessageListener listener);
}
