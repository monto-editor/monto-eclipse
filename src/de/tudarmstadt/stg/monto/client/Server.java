package de.tudarmstadt.stg.monto.client;

import java.util.Optional;
import java.util.function.Function;

import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public interface Server extends Function<VersionMessage,Optional<ProductMessage>> {
	
}
