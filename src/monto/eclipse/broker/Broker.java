package monto.eclipse.broker;

public abstract class Broker implements AutoCloseable {
	
	private Process brokerProcess;

	protected abstract Process startProcess() throws Exception;
	
	public void start() throws Exception {
		if(brokerProcess != null)
			throw new IllegalStateException("The broker is allready running");
		brokerProcess = startProcess();
	}
	
	@Override
	public void close() throws Exception {
		if(brokerProcess != null) {
			brokerProcess.destroy();
			brokerProcess = null;
		}
	}
}
