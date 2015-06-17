package monto.eclipse.sink;

import java.util.concurrent.Executor;

import monto.eclipse.message.Language;
import monto.eclipse.message.Product;
import monto.eclipse.message.ProductMessage;
import monto.eclipse.message.Source;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;

public class DocumentSink extends Document implements Sink {

	private Source source;
	private Product product;
	private Executor executor;
	private Language language;

	DocumentSink(Source source, Product product, Language language) {
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
