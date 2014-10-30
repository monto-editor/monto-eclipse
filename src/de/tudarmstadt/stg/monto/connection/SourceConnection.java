package de.tudarmstadt.stg.monto.connection;

import java.util.List;

import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class SourceConnection {
	
	private OutgoingConnection connection;

	public SourceConnection(OutgoingConnection connection) {
		this.connection = connection;
	}
	
	public void connect() throws Exception {
		connection.connect();
	}

	public void sendVersionMessage(Source source, Language language,
			Contents contents, List<Selection> selections) throws Exception {
		sendVersionMessage(new VersionMessage(source, language, contents, selections));
	}
	
	public void sendVersionMessage(VersionMessage message) throws Exception {
		connection.sendMessage(VersionMessage.encode(message).toJSONString());
	}
	
	public void close() throws Exception {
		connection.close();
	}

}
