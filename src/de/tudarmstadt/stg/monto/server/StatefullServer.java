package de.tudarmstadt.stg.monto.server;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public abstract class StatefullServer extends AbstractServer {

	private Map<Source,VersionMessage> versions = new HashMap<>();

	@Override
	public void onVersionMessage(VersionMessage message) {
		if(isRelevant(message))
			versions.put(message.getSource(),message);
	}
	
	protected abstract boolean isRelevant(VersionMessage message);

	public VersionMessage getVersionMessage(Source source) {
		return versions.get(source);
	}
	
	public void resetVersionMessage(Source source) {
		versions.remove(source);
	}
}
