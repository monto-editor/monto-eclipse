package monto.eclipse;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import monto.eclipse.connection.Discovery;
import monto.eclipse.connection.Publish;
import monto.eclipse.connection.PublishConfiguration;
import monto.eclipse.connection.PublishSource;
import monto.eclipse.connection.RequestResponse;
import monto.service.configuration.BooleanSetting;
import monto.service.configuration.Setting;
import monto.service.configuration.Configuration;
import monto.service.configuration.NumberSetting;
import monto.service.configuration.Option;
import monto.service.configuration.TextSetting;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;
import monto.service.discovery.ServiceDescription;
import monto.service.source.SourceMessage;
import monto.service.types.ServiceId;

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
		config.bind();
		
		discover = new Discovery(new RequestResponse(ctx,"tcp://localhost:5005"));
		discover.connect();
		
		restoreOptions();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void restoreOptions() {
		IPreferenceStore store = getDefault().getPreferenceStore();
		discover(new DiscoveryRequest())
			.ifPresent(resp -> {
				for(ServiceDescription service : resp.getServices()) {
					for(Option option : service.getOptions()) {
						restoreOption(service, option,store);
					}
				}
			});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <A> void restoreOption(ServiceDescription service, Option<A> option, IPreferenceStore store) {
		String serviceID = service.getServiceId().toString();
		Activator.debug("restore option: %s", option);
		option.<Void>match(
			booleanOption -> {
				Setting conf = new BooleanSetting(booleanOption.getOptionId(), store.getBoolean(serviceID + booleanOption.getOptionId()));
				configure(service.getServiceId(), conf);
				return null;
			},
			numberOption -> {
				Setting conf = new NumberSetting(numberOption.getOptionId(), store.getInt(serviceID + numberOption.getOptionId()));
				configure(service.getServiceId(), conf);
				return null;
			},
			textOption -> {
				Setting conf = new TextSetting(textOption.getOptionId(), store.getString(serviceID + textOption.getOptionId()));
				configure(service.getServiceId(), conf);
				return null;
			},
			xorOption -> {
				Setting conf = new TextSetting(xorOption.getOptionId(), store.getString(serviceID) + xorOption.getOptionId());
				configure(service.getServiceId(), conf);
				return null;
			},
			optionGroup -> {
				optionGroup.getMembers().forEach(opt -> restoreOption(service,opt,store));
				return null;
			});
	}

	public static Sink sink(String service) {
		return new Sink(new Subscribe(ctx, "tcp://localhost:5001"), service);
	}
	
	public static Optional<DiscoveryResponse> discover(DiscoveryRequest request) {
		return getDefault().discover.discoveryRequest(request);
	}
	
	public static <T> void configure(Configuration config) {
		getDefault().config.sendMessage(config);
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> void configure(ServiceId serviceId, Setting ... confs) {
		configure(new Configuration(serviceId,Arrays.asList(confs)));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundle) throws Exception {
		source.close();
		config.close();
		discover.close();
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

	public static void sendMessage(SourceMessage version) {
		getDefault().source.sendMessage(version);
	}
}
