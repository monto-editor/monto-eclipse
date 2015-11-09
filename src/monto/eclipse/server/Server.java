package monto.eclipse.server;

import java.util.List;

import monto.eclipse.Either;
import monto.service.message.Message;
import monto.service.message.ProductMessage;

interface Server {
	public Either<Exception,ProductMessage> onMessage(List<Message> msg) throws Exception;
	
	public void stop();
}
