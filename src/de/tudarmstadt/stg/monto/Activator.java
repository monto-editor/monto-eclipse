package de.tudarmstadt.stg.monto;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.tudarmstadt.stg.monto.client.LineSplitter;
import de.tudarmstadt.stg.monto.client.MockMontoClient;
import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.client.ReverseContent;

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
		client = new MockMontoClient()
			.addServer(new ReverseContent())
			.addServer(new LineSplitter());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

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

	public static void debug(String msg) {
		getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, msg));
	}

	public static void error(Exception e) {
		error(null, e);
	}
	
	public static void error(String msg, Exception e) {
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
	}
}
