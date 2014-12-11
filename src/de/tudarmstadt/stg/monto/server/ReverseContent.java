package de.tudarmstadt.stg.monto.server;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class ReverseContent extends AbstractServer {

	private final Product product = new Product("Reversed");

	@Override
	public void receiveVersionMessage(VersionMessage version) {
		final Contents reversed =
				new StringContent(
				new StringBuilder(
						version.getContent().toString()
						).reverse().toString());
		emitProductMessage(
				new ProductMessage(
						version.getVersionId(),
						new LongKey(1),
						version.getSource(), 
						product, 
						version.getLanguage(), 
						reversed));
	}

	@Override
	protected boolean isRelveant(VersionMessage message) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
