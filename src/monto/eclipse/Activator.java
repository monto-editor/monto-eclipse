package monto.eclipse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import monto.eclipse.demultiplex.ProductCache;
import monto.eclipse.demultiplex.SinkDemultiplexer;
import monto.ide.SinkSocket;
import monto.ide.SourceSocket;
import monto.service.command.CommandMessage;
import monto.service.configuration.BooleanSetting;
import monto.service.configuration.Configuration;
import monto.service.configuration.NumberSetting;
import monto.service.configuration.Option;
import monto.service.configuration.Setting;
import monto.service.configuration.TextSetting;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;
import monto.service.discovery.ServiceDescription;
import monto.service.gson.MessageFromIde;
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

  private SourceSocket source;
  private SinkSocket sink;
  private static Context ctx;
  private SinkDemultiplexer demultiplexer;
  private ProductCache<DiscoveryResponse> discoveryResponseCache;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundle) throws Exception {
    super.start(bundle);
    plugin = this;

    ctx = ZMQ.context(1);

    source = new SourceSocket(ctx, "tcp://localhost:5000");
    source.connect();

    sink = new SinkSocket(ctx, "tcp://localhost:5001");
    sink.connect();

    discoveryResponseCache = new ProductCache<>("discoveryResponse");
    discoveryResponseCache.setTimeout(500);
    demultiplexer = new SinkDemultiplexer(sink).setDiscoveryCache(discoveryResponseCache).start();

    discover(DiscoveryRequest.create()).ifPresent(discoverResponse -> {
      sendConfigurationsFromStore(discoverResponse.get());
    });
  }

  public SinkDemultiplexer getDemultiplexer() {
    return demultiplexer;
  }

  public Optional<DiscoveryResponse> discover(DiscoveryRequest request) {
    source.send(MessageFromIde.discover(request));
    discoveryResponseCache.invalidateProduct();
    Optional<DiscoveryResponse> maybeDiscoverResponse = discoveryResponseCache.getProduct();
    maybeDiscoverResponse.ifPresent(discoverResponse -> {
      discoverResponse.get().forEach(serviceDescription -> {
        setStoreDefaults(serviceDescription.getServiceId(), serviceDescription.getOptions(),
            getPreferenceStore());
      });
    });
    return maybeDiscoverResponse;
  }

  @SuppressWarnings("rawtypes")
  private void setStoreDefaults(ServiceId serviceId, List<Option> options, IPreferenceStore store) {
    for (Option<?> option : options) {
      option.matchVoid(booleanOption -> {
        store.setDefault(Activator.getStoreKey(serviceId, option), booleanOption.getDefaultValue());
      }, numberOption -> {
        store.setDefault(Activator.getStoreKey(serviceId, option), numberOption.getDefaultValue());
      }, textOption -> {
        store.setDefault(Activator.getStoreKey(serviceId, option), textOption.getDefaultValue());
      }, xorOption -> {
        store.setDefault(Activator.getStoreKey(serviceId, option), xorOption.getDefaultValue());
      }, optionGroup -> {
        setStoreDefaults(serviceId, optionGroup.getMembers(), store);
      });
    }
  }


  public static void sendConfigurationsFromStore(List<ServiceDescription> serviceDescriptions) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    List<Configuration> configurations = serviceDescriptions.stream().map(serviceDescription -> {
      return new Configuration(serviceDescription.getServiceId(),
          getStoreSettings(serviceDescription.getServiceId(), serviceDescription.getOptions(),
              store).collect(Collectors.toList()));
    }).collect(Collectors.toList());
    Activator.getDefault().source.send(MessageFromIde.config(configurations));
  }


  @SuppressWarnings("rawtypes")
  private static Stream<Setting> getStoreSettings(ServiceId serviceId, List<Option> options,
      IPreferenceStore store) {
    return options.stream().flatMap(option -> {
      return ((Option<?>) option).match(booleanOption -> {
        return Stream.of(new BooleanSetting(booleanOption.getOptionId(),
            store.getBoolean(Activator.getStoreKey(serviceId, booleanOption))));
      }, numberOption -> {
        return Stream.of(new NumberSetting(numberOption.getOptionId(),
            store.getInt(Activator.getStoreKey(serviceId, numberOption))));
      }, textOption -> {
        return Stream.of(new TextSetting(textOption.getOptionId(),
            store.getString(Activator.getStoreKey(serviceId, textOption))));
      }, xorOption -> {
        return Stream.of(new TextSetting(xorOption.getOptionId(),
            store.getString(Activator.getStoreKey(serviceId, xorOption))));
      }, optionGroup -> {
        return getStoreSettings(serviceId, optionGroup.getMembers(), store);
      });
    });
  }

  @SuppressWarnings("rawtypes")
  public static String getStoreKey(ServiceId serviceId, Option option) {
    return serviceId.toString() + option.toString();
  }

  public static void sendSourceMessage(SourceMessage message) {
    getDefault().source.send(MessageFromIde.source(message));
  }

  public static void sendCommandMessage(CommandMessage commandMessage) {
    getDefault().source.send(MessageFromIde.command(commandMessage));
  }

  public static Activator getDefault() {
    return plugin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundle) throws Exception {
    demultiplexer.stop();
    source.close();
    // demultiplexer closes sink once thread shuts down
    ctx.close();
    plugin = null;
    super.stop(bundle);
  }

  public static void debug(String msg, Object... formatArgs) {
    getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, String.format(msg, formatArgs)));
  }

  public static void error(Exception e) {
    error(null, e);
  }

  public static void error(String msg, Exception e) {
    getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
  }

  public static void info(String msg) {
    getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, msg));
  }

  public static void warning(String msg) {
    getDefault().getLog().log(new Status(Status.WARNING, PLUGIN_ID, msg));
  }
}
