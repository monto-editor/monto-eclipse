package de.tudarmstadt.stg.monto.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.stg.monto.message.Dependency;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Server;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public abstract class StatefullServer implements Server, ProductMessageListener {

	protected Map<Tuple2<Source,Language>,VersionMessage> versions = new HashMap<>();
	protected Map<Tuple3<Source,Language,Product>,ProductMessage> products = new HashMap<>();
	protected List<ProductMessageListener> productMessageListeners = new ArrayList<>();
	
	@Override
	public void onVersionMessage(VersionMessage message) {
		removeInvalid(message);
		if(isRelevant(message)) {
			versions.put(new Tuple2<>(message.getSource(),message.getLanguage()),message);
			onMessage(message);
		}
	}
	
	protected VersionMessage getVersionMessage(Source source, Language language) {
		return versions.get(new Tuple2<>(source, language));
	}
	
	@Override
	public void onProductMessage(ProductMessage message) {
		removeInvalid(message);
		if(isRelevant(message)) {
			products.put(new Tuple3<>(message.getSource(),message.getLanguage(),message.getProduct()), message);
			onMessage(message);
		}
	}
	
	protected ProductMessage getProductMessage(Source source, Language language, Product product) {
		return products.get(new Tuple3<>(source, language, product));
	}
	
	private void removeInvalid(Message message) {
		for(Dependency dependency : message.getInvalid())
			dependency.match(
					version -> versions.remove(new Tuple2<>(version.getSource(),version.getLanguage())),
					product -> products.remove(new Tuple3<>(product.getSource(),product.getLanguage(),product.getProduct())));
	}
	
	protected abstract boolean isRelevant(VersionMessage message);
	protected abstract boolean isRelevant(ProductMessage message);
	protected abstract void onMessage(Message message);
	
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
