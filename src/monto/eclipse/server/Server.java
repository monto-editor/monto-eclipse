package monto.eclipse.server;

import java.util.List;

import monto.eclipse.Either;
import monto.service.product.ProductMessage;
import monto.service.types.Message;

interface Server {
  public Either<Exception, ProductMessage> onMessage(List<Message> msg) throws Exception;

  public void stop();
}
