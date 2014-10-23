package de.tudarmstadt.stg.monto.sink;

import java.util.concurrent.Executor;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;

import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Source;

public class Sink extends Document implements ProductMessageListener {

	private Source source;
	private Product product;
	private Executor executor;

	public Sink(Source source, Product product) {
		this.source = source;
		this.product = product;
		this.executor = Display.getDefault()::asyncExec;
	}
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void onProductMessage(ProductMessage message) {
		if(message.getSource().equals(this.source)
		&& message.getProduct().equals(this.product)) {
			
			// This needs to be in the ui thread since this causes a change to the display
			executor.execute(
				() -> this.set(message.getContents().toString())
			);
		}
	}
}
