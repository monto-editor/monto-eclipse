package monto.eclipse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import monto.eclipse.connection.Pair;
import monto.eclipse.connection.Publish;
import monto.eclipse.connection.PublishSource;
import monto.eclipse.connection.Sink;
import monto.eclipse.connection.Subscribe;
import monto.eclipse.message.MessageListener;
import monto.eclipse.message.ProductMessage;
import monto.eclipse.message.ProductRegistry;
import monto.eclipse.message.Source;
import monto.eclipse.message.VersionMessage;
import monto.eclipse.message.ProductRegistry.ProductItem;
import monto.eclipse.profiling.Profiler;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "monto"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private Profiler profiler;
	private Set<MessageListener> messageListeners = new HashSet<>();
	private ProductRegistry products = new ProductRegistry();
	private PublishSource source;
	private Sink sink;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundle) throws Exception {
		super.start(bundle);
		plugin = this;
		
		String profFile = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk-mm-ss"))+".csv";
		profiler = new Profiler(new PrintWriter(new BufferedWriter(new FileWriter(profFile))));
		
		Context ctx = ZMQ.context(1);
		
		source = new PublishSource(new Publish(ctx, "tcp://localhost:5000"));
		source.connect();

		sink = new Sink(new Subscribe(ctx, "tcp://localhost:5001")) {
			@Override public void onMessage(ProductMessage msg) {
				for(MessageListener listener : messageListeners)
					listener.onMessage(msg);
				products.registerProduct(msg.getSource(),msg.getProduct(),msg.getLanguage());
			}
		};
		sink.fork();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundle) throws Exception {
		source.close();
		sink.stop();
		profiler.close();
		plugin = null;
		super.stop(bundle);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	public static Profiler getProfiler() {
		return getDefault().profiler;
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

	public static void addMessageListener(MessageListener listener) {
		getDefault().messageListeners.add(listener);
	}
	
	public static void removeMessageListener(MessageListener listener) {
		getDefault().messageListeners.remove(listener);
	}
	
	public static void sendMessage(VersionMessage version) {
		getDefault().source.sendMessage(version);
	}
	
	public static Set<ProductItem> availableProducts(Source source) {
		return getDefault().products.availableProducts(source);
	}
}
