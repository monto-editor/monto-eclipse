package de.tudarmstadt.stg.monto;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.tudarmstadt.stg.monto.client.LineSplitter;
import de.tudarmstadt.stg.monto.client.MockClient;
import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.client.ReverseContent;
import de.tudarmstadt.stg.monto.token.JavaTokenizer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "monto"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private MontoClient client;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings("resource")
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

//		client = new ZMQClient();
		client = new MockClient()
			.addServer(new ReverseContent())
			.addServer(new LineSplitter())
			.addServer(new JavaTokenizer());
		client.connect();
		client.listening();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		client.close();
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public MontoClient getMontoClient() {
		return client;
	}
	
	public static void debug(String msg, Object ... formatArgs) {
		getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, String.format(msg,formatArgs)));
	}

	public static void error(Exception e) {
		error(null, e);
	}
	
	public static void error(String msg, Exception e) {
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
	}
}
