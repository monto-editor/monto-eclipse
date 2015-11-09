package monto.eclipse;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import monto.eclipse.connection.Discovery;
import monto.eclipse.connection.Publish;
import monto.eclipse.connection.PublishConfiguration;
import monto.eclipse.connection.PublishSource;
import monto.eclipse.connection.RequestResponse;
import monto.eclipse.connection.Sink;
import monto.eclipse.connection.Subscribe;
import monto.eclipse.connection.SubscribeDiscover;
import monto.service.configuration.Configuration;
import monto.service.configuration.ServiceConfiguration;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;
import monto.service.message.VersionMessage;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "monto"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private PublishSource source;
	private PublishConfiguration config;
	private Discovery discover;
	private SubscribeDiscover discoverResponse;
	private static Context ctx;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundle) throws Exception {
		super.start(bundle);
		plugin = this;
		
		ctx = ZMQ.context(1);
		
		source = new PublishSource(new Publish(ctx, "tcp://localhost:5000"));
		source.connect();
		
		config = new PublishConfiguration(new Publish(ctx,"tcp://localhost:5007"));
		config.connect();
		
		discover = new Discovery(new RequestResponse(ctx,"tcp://localhost:5005"));
		discover.connect();
		
	}
	
	public static Sink sink(String service) {
		return new Sink(new Subscribe(ctx, "tcp://localhost:5001"), service);
	}
	
	public static Optional<DiscoveryResponse> discover(DiscoveryRequest request) {
		return getDefault().discover.discoveryRequest(request);
	}
	
	public static <T> void configure(ServiceConfiguration config) {
		getDefault().config.sendMessage(config);
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> void configure(String serviceId, Configuration ... confs) {
		configure(new ServiceConfiguration(serviceId,Arrays.asList(confs)));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundle) throws Exception {
		source.close();
		config.close();
		discover.close();
		discoverResponse.close();
		ctx.close();
		plugin = null;
		super.stop(bundle);
	}

	public static Activator getDefault() {
		return plugin;
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

	public static void sendMessage(VersionMessage version) {
		getDefault().source.sendMessage(version);
	}
}
