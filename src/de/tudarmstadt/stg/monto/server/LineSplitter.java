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

public class LineSplitter extends AbstractServer {

	public LineSplitter(Pair connection) {
		super(connection);
	}

	private final Product product = new Product("Splitted");

	@Override
	public Either<Exception,ProductMessage> onMessage(List<Message> messages) {
		return Messages.getVersionMessage(messages).map(version -> {
			final Contents splitted = new StringContent(version.getContent().toString().replace(' ', '\n'));
		
			return new ProductMessage(
				version.getVersionId(),
				new LongKey(1),
				version.getSource(), 
				product, 
				version.getLanguage(),
				splitted);
		});
	}
}
