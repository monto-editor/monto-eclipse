package de.tudarmstadt.stg.monto.server;

public interface ProductMessageProducer {
	public void addProductMessageListener(ProductMessageListener listener);
	public void removeProductMessageListener(ProductMessageListener listener);
}
