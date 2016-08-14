package monto.eclipse.demultiplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import monto.eclipse.Activator;
import monto.ide.SinkSocket;
import monto.service.command.CommandUpdate;
import monto.service.discovery.DiscoveryResponse;
import monto.service.product.ProductMessage;
import monto.service.types.Product;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class SinkDemultiplexer {
  private SinkSocket sink;
  private Thread thread;
  private boolean running;

  private Map<Product, List<Consumer<ProductMessage>>> productListeners;
  private Map<String, List<Consumer<CommandUpdate>>> commandUpdateListeners;
  private List<Consumer<DiscoveryResponse>> discoveryListeners;

  public SinkDemultiplexer(SinkSocket sink) {
    this.sink = sink;
    this.productListeners = new HashMap<>();
    this.commandUpdateListeners = new HashMap<>();
    this.discoveryListeners = new ArrayList<>();
  }

  public void addProductListener(Product product, Consumer<ProductMessage> consumer) {
    if (!productListeners.containsKey(product)) {
      productListeners.put(product, new ArrayList<>());
    }
    productListeners.get(product).add(consumer);
  }
  
  public void removeProductListener(Product product, Consumer<Product> consumer) {
    if (productListeners.containsKey(product)) {
      productListeners.get(product).remove(consumer);
    }
  }

  public void addCommandUpdateListener(String tag, Consumer<CommandUpdate> consumer) {
    if (!productListeners.containsKey(tag)) {
      commandUpdateListeners.put(tag, new ArrayList<>());
    }
    commandUpdateListeners.get(tag).add(consumer);
  }
  
  public void removeCommandUpdateListener(String tag, Consumer<CommandUpdate> consumer) {
    if (productListeners.containsKey(tag)) {
      productListeners.get(tag).remove(consumer);
    }
  }

  public void addDiscoveryListener(Consumer<DiscoveryResponse> consumer) {
    discoveryListeners.add(consumer);
  }

  public void removeDiscoveryListener(Consumer<DiscoveryResponse> consumer) {
    discoveryListeners.remove(consumer);
  }

  public SinkDemultiplexer start() {
    sink.connect();
    running = true;
    thread = new Thread() {
      @Override
      public void run() {
        try {
          while (running) {
            sink.receive(productMessage -> {
              Activator.debug("received ProductMessage: %s", productMessage);
              List<Consumer<ProductMessage>> listeners =
                  productListeners.get(productMessage.getProduct());
              // TODO: check, if source and language of productMessage match the opened file?

              if (listeners == null) {
                Activator.debug("Ignoring ProductMessage %s, because not listener wants it", productMessage);
              } else {
                for (Consumer<ProductMessage> consumer : listeners) {
                  consumer.accept(productMessage);
                }                
              }
            }, commandUpdate -> {
              Activator.debug("Received CommandUpdate: %s", commandUpdate);
              List<Consumer<CommandUpdate>> listeners =
                  commandUpdateListeners.get(commandUpdate.getTag());

              if (listeners == null) {
                Activator.debug("Ignoring CommandUpdate %s, because not listener wants it", commandUpdate);
              } else {
                for (Consumer<CommandUpdate> consumer : listeners) {
                  consumer.accept(commandUpdate);
                }                
              }
            }, discoveryResponse -> {
              Activator.debug("Received DiscoveryResponse: %s", discoveryResponse);
              for (Consumer<DiscoveryResponse> consumer : discoveryListeners) {
                consumer.accept(discoveryResponse);
              }
            });
          }
          sink.close();
        } catch (Exception e) {
          Activator.error(e);
        }
      }
    };
    thread.start();

    return this;
  }

  public void stop() throws InterruptedException {
    running = false;
    thread.join();
  }
}
