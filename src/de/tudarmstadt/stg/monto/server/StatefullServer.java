package de.tudarmstadt.stg.monto.server;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.LatestMessages;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Server;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public abstract class StatefullServer implements Server {

	private LatestMessages<VersionMessage> versions = new LatestMessages<>();
	protected List<ProductMessageListener> productMessageListeners = new ArrayList<>();
	
	@Override
	public void onVersionMessage(VersionMessage message) {
		if(isRelevant(message)) {
			versions.addMessage(message)
				.isNewer(msg -> receiveVersionMessage(msg));
		}
	}
	
	protected abstract void receiveVersionMessage(VersionMessage msg);
	protected abstract boolean isRelevant(VersionMessage message);
	protected VersionMessage getLatestVersionMessage(Source source) {
		return versions.getNewest(source);
	}
	
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
