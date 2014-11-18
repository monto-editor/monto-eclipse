package de.tudarmstadt.stg.monto.server;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Server;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public abstract class AbstractServer implements Server {

	protected List<ProductMessageListener> productMessageListeners = new ArrayList<>();
	
	public final void onVersionMessage(VersionMessage message) {
		if(isRelveant(message)) {
			receiveVersionMessage(message);
		}
	};
	
	protected abstract boolean isRelveant(VersionMessage message);
	protected abstract void receiveVersionMessage(VersionMessage message);

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
