package de.tudarmstadt.stg.monto.sink;

import de.tudarmstadt.stg.monto.message.VersionMessage;


public interface VersionMessageListener {
	public void onVersionMessage(VersionMessage message);
}
