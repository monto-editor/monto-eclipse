package de.tudarmstadt.stg.monto;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import de.tudarmstadt.stg.monto.connection.Connection;
import de.tudarmstadt.stg.monto.connection.ServerConnection;
import de.tudarmstadt.stg.monto.connection.SinkConnection;
import de.tudarmstadt.stg.monto.connection.SourceConnection;
import de.tudarmstadt.stg.monto.java8.JavaOutliner;
import de.tudarmstadt.stg.monto.java8.JavaParser;
import de.tudarmstadt.stg.monto.java8.JavaTokenizer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "monto"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private SourceConnection sourceConnection;
	private ServerConnection serverConnection;
	private SinkConnection sinkConnection;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundle) throws Exception {
		super.start(bundle);
		plugin = this;
		
		Context context = ZMQ.context(1);
		sourceConnection = Connection.createSourceConnection(context);
		serverConnection = Connection.createServerConnection(context);
		sinkConnection = Connection.createSinkConnection(context);
		
		sourceConnection.connect();
		serverConnection.connect();
		sinkConnection.connect();
		
		serverConnection.listening();
		sinkConnection.listening();
		
		serverConnection.addServer(new JavaTokenizer());
		serverConnection.addServer(new JavaParser());
		JavaOutliner javaOutliner = new JavaOutliner();
		serverConnection.addServer(javaOutliner);
		sinkConnection.addSink(javaOutliner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundle) throws Exception {

		sourceConnection.close();
		serverConnection.close();
		sinkConnection.close();
		
		plugin = null;
		super.stop(bundle);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	public static SourceConnection getSourceConnection() {
		return getDefault().sourceConnection;
	}

	public static ServerConnection getServerConnection() {
		return getDefault().serverConnection;
	}

	public static SinkConnection getSinkConnection() {
		return getDefault().sinkConnection;
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
