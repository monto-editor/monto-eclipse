package de.tudarmstadt.stg.monto.server;

import java.util.List;

import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.ProductMessage;

public interface Server {
	public Either<Exception,ProductMessage> onMessage(List<Message> msg) throws Exception;
	public void fork();
	public void stop();
}
