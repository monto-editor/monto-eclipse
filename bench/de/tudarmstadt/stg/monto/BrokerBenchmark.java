package de.tudarmstadt.stg.monto;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import de.tudarmstadt.stg.monto.broker.Broker;
import de.tudarmstadt.stg.monto.connection.Connection;
import de.tudarmstadt.stg.monto.connection.SinkConnection;
import de.tudarmstadt.stg.monto.connection.SourceConnection;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;

public class BrokerBenchmark {
	
	private static final String loremIpsum 
			= "Lorem ipsum dolor sit amet, consectetur adipisici elit,"
			+ " sed eiusmod tempor incidunt ut labore et dolore magna aliqua. "
			+ "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris "
			+ "nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit "
			+ "in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur "
			+ "sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt "
			+ "mollit anim id est laborum."
			+ "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse "
			+ "molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero "
			+ "eros et accumsan et iusto odio dignissim qui blandit praesent luptatum "
			+ "zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor "
			+ "sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod "
			+ "tincidunt ut laoreet dolore magna aliquam erat volutpat."
			+ "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper "
			+ "suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem "
			+ "vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, "
			+ "vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto "
			+ "odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te "
			+ "feugait nulla facilisi.";
	
    protected Broker broker;
	protected Process reverseServer;
	protected SourceConnection sourceConnection;
	protected SinkConnection sinkConnection;
	protected static LongKey id = new LongKey(0);

	protected static final VersionMessage message = 
			new VersionMessage(
					id,
					new Source("lorem ipsum"),
					new Language("text"),
					new StringContent(loremIpsum));
	
	public BrokerBenchmark(Broker broker) {
		this.broker = broker;
	}

	public void setUp() throws Exception {
		broker.start();
		String reverseServerCommand = System.getProperty("reverse.server");
		if(reverseServerCommand == null)
			throw new Exception("Please set the \"reverse.server\" property");
		reverseServer = new ProcessBuilder(reverseServerCommand).start();
		
		Context context = ZMQ.context(1);
		sourceConnection = Connection.createSourceConnection(context);
		sinkConnection = Connection.createSinkConnection(context);
		
		sourceConnection.connect();
		sinkConnection.connect();
	}
	
	public void testBroker() throws Exception {
		sourceConnection.sendVersionMessage(message);
		sinkConnection.receiveProductMessage();
	}

	public void tearDown() throws Exception {
		sourceConnection.close();
		sinkConnection.close();
		reverseServer.destroy();
		broker.close();
	}
}
