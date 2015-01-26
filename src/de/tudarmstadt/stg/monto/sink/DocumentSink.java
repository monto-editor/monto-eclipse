package de.tudarmstadt.stg.monto.sink;

import java.util.concurrent.Executor;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;

import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Source;

public class DocumentSink extends Document implements Sink {

	private Source source;
	private Product product;
	private Executor executor;
	private Language language;

	public DocumentSink(Source source, Product product, Language language) {
		this.source = source;
		this.product = product;
		this.language = language;
		this.executor = Display.getDefault()::asyncExec;
	}
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void onMessage(ProductMessage message) {
		if(message.getSource().equals(source) && message.getProduct().equals(product) && message.getLanguage().equals(language)) {
			executor.execute(() -> this.set(message.getContents().toString()));
		}
	}
}
