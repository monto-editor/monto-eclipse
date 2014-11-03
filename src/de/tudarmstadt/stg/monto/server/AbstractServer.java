package de.tudarmstadt.stg.monto.server;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Server;

public abstract class AbstractServer implements Server {

	protected List<ProductMessageListener> productMessageListeners = new ArrayList<>();
	
	@Override
	public void addProductMessageListener(ProductMessageListener listener) {
		productMessageListeners.add(listener);
	}
	
	@Override
	public void removeProductMessageListener(ProductMessageListener listener) {
		productMessageListeners.remove(listener);
	}

	protected final void emitProductMessage(ProductMessage message) {
		productMessageListeners.forEach(listener -> listener.onProductMessage(message));
	}
}
