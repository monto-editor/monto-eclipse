package de.tudarmstadt.stg.monto.server;

import de.tudarmstadt.stg.monto.message.ProductMessage;

public interface ProductMessageListener {
	public void onProductMessage(ProductMessage message);
}
