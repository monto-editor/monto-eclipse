package de.tudarmstadt.stg.monto.client;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.stg.monto.message.ProductMessageListener;

public abstract class AbstractMontoClient implements MontoClient {

	protected List<ProductMessageListener> listeners = new ArrayList<>();

	@Override
	public void addProductMessageListener(ProductMessageListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeProductMessageListener(ProductMessageListener listener) {
		listeners.remove(listener);
	}

}
