package de.tudarmstadt.stg.monto.client;

import java.util.function.Function;

import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public interface Server extends Function<VersionMessage,ProductMessage> {
	
	public Product getProduct();
	
}
