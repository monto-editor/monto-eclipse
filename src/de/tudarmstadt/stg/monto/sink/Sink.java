package de.tudarmstadt.stg.monto.sink;

import de.tudarmstadt.stg.monto.message.ProductMessage;

public interface Sink {
	public void onProductMessage(ProductMessage message);
}
