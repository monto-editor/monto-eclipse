package de.tudarmstadt.stg.monto.broker;

public class PythonBroker extends Broker {
	
	private ProcessBuilder processBuilder;
	
	private static String init() throws Exception {
		String brokerCommand = System.getProperty("monto.python");
		if(brokerCommand == null)
			throw new Exception("Please set the \"monto.python\" system property");
		return brokerCommand;
	}
	
	public PythonBroker() throws Exception {
		this(init());
	}
	
	public PythonBroker(String brokerCommand) {
		processBuilder = new ProcessBuilder(brokerCommand);
	}

	@Override
	protected Process startProcess() throws Exception {
		return processBuilder.start();
	}
}
