package de.tudarmstadt.stg.monto.message;

import de.tudarmstadt.stg.monto.server.ProductMessageProducer;
import de.tudarmstadt.stg.monto.sink.VersionMessageListener;


public interface Server extends VersionMessageListener, ProductMessageProducer {

}
