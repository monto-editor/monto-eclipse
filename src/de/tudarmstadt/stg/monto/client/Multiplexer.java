package de.tudarmstadt.stg.monto.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.language.Language;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;

public class Multiplexer implements MontoClient {

	private List<MontoClient> childs = new ArrayList<>();
	
	public void addClient(MontoClient client) {
		this.childs.add(client);
	}
	
	public void removeClient(MontoClient client) {
		this.childs.remove(client);
	}
	
	@Override
	public void sendVersionMessage(Source source, Language language,
			Contents contents, Selection selection) {
		childs.forEach((client) -> client.sendVersionMessage(source, language, contents, selection));
	}

	@Override
	public void addProductMessageListener(ProductMessageListener listener) {
		childs.forEach((client) -> client.addProductMessageListener(listener));
	}

	@Override
	public void removeProductMessageListener(ProductMessageListener listener) {
		childs.forEach((client) -> client.removeProductMessageListener(listener));
	}

}
