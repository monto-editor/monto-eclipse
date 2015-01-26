package de.tudarmstadt.stg.monto.server;

import java.util.List;

import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.connection.AbstractServer;
import de.tudarmstadt.stg.monto.connection.Pair;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Message;
import de.tudarmstadt.stg.monto.message.Messages;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;

public class ReverseContent extends AbstractServer {

	public ReverseContent(Pair connection) {
		super(connection);
	}

	private final Product product = new Product("Reversed");

	@Override
	public Either<Exception,ProductMessage> onMessage(List<Message> msg) throws Exception {
		return Messages.getVersionMessage(msg).map(
			version -> {
				final Contents reversed =
						new StringContent(
						new StringBuilder(
								version.getContent().toString()
								).reverse().toString());
				return new ProductMessage(
					version.getVersionId(),
					new LongKey(1),
					version.getSource(), 
					product, 
					version.getLanguage(), 
					reversed);
			}
		);		
	}

}
