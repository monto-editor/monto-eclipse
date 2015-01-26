package de.tudarmstadt.stg.monto;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import de.tudarmstadt.stg.monto.connection.Pair;
import de.tudarmstadt.stg.monto.connection.Publish;
import de.tudarmstadt.stg.monto.connection.PublishSource;
import de.tudarmstadt.stg.monto.connection.Sink;
import de.tudarmstadt.stg.monto.connection.Subscribe;
import de.tudarmstadt.stg.monto.java8.JavaCodeCompletion;
import de.tudarmstadt.stg.monto.java8.JavaOutliner;
import de.tudarmstadt.stg.monto.java8.JavaParser;
import de.tudarmstadt.stg.monto.java8.JavaTokenizer;
import de.tudarmstadt.stg.monto.message.MessageListener;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductRegistry;
import de.tudarmstadt.stg.monto.message.ProductRegistry.ProductItem;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.profiling.Profiler;
import de.tudarmstadt.stg.monto.server.Server;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "monto"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private Profiler profiler;
	private List<Server> servers;
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
		
		servers = Arrays.asList(
				new JavaTokenizer(new Pair(ctx, "tcp://localhost:5010")),
				new JavaParser(new Pair(ctx, "tcp://localhost:5011")),
				new JavaOutliner(new Pair(ctx, "tcp://localhost:5012")),
				new JavaCodeCompletion(new Pair(ctx, "tcp://localhost:5013"))
				//new JsonPrettyPrinter(new Connection(ctx, "tcp://localhost:5004"))
				);
		servers.forEach(server -> server.fork());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundle) throws Exception {
		source.close();
		sink.stop();
		servers.forEach(server -> server.stop());
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
