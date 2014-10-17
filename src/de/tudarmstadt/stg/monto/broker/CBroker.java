package de.tudarmstadt.stg.monto.broker;

import java.io.IOException;

public class CBroker extends Broker {
	
	private ProcessBuilder brokerBuilder;
	
	private static String init() throws Exception {
		String brokerCommand = System.getProperty("monto.c");
		if(brokerCommand == null)
			throw new Exception("Please set the \"monto.c\" system property to the path of the c monto broker");
		return brokerCommand;
	}
	
	public CBroker() throws Exception {
		this(init());
	}
	
	public CBroker(String brokerCommand) {
		brokerBuilder = new ProcessBuilder(brokerCommand);
	}
	
	@Override
	protected Process startProcess() throws IOException {
		return brokerBuilder.start();
	}
}
