package monto.eclipse.server;

import java.util.List;

import monto.eclipse.Either;
import monto.eclipse.message.Message;
import monto.eclipse.message.ProductMessage;

interface Server {
	public Either<Exception,ProductMessage> onMessage(List<Message> msg) throws Exception;
	
	public void stop();
}
