package monto.eclipse.demultiplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.javatuples.Pair;

import monto.eclipse.Activator;
import monto.ide.SinkSocket;
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

  private Map<Product, List<Pair<Consumer<ProductMessage>, Object>>> productListeners;
  private List<Pair<Consumer<DiscoveryResponse>, Object>> discoveryListeners;

  public SinkDemultiplexer(SinkSocket sink) {
    this.sink = sink;
    this.productListeners = new HashMap<>();
    this.discoveryListeners = new ArrayList<>();
  }

  public void addProductListener(Product product, Consumer<ProductMessage> consumer,
      Object identifier) {
    synchronized (productListeners) {
      if (!productListeners.containsKey(product)) {
        productListeners.put(product, new ArrayList<>());
      }
      productListeners.get(product).add(new Pair<>(consumer, identifier));
    }
  }

  public void removeProductListener(Product product, Object identifier) {
    synchronized (productListeners) {
      if (productListeners.containsKey(product)) {
        for (Iterator<Pair<Consumer<ProductMessage>, Object>> iterator =
            productListeners.get(product).iterator(); iterator.hasNext();) {
          Pair<Consumer<ProductMessage>, Object> pair = iterator.next();
          if (pair.getValue1().equals(identifier)) {
            iterator.remove();
          }
        }
      }
    }
  }

  public void addDiscoveryListener(Consumer<DiscoveryResponse> consumer, Object identifier) {
    synchronized (discoveryListeners) {
      discoveryListeners.add(new Pair<>(consumer, identifier));
    }
  }

  public void removeDiscoveryListener(Object identifier) {
    synchronized (discoveryListeners) {
      for (Iterator<Pair<Consumer<DiscoveryResponse>, Object>> iterator =
          discoveryListeners.iterator(); iterator.hasNext();) {
        Pair<Consumer<DiscoveryResponse>, Object> pair = iterator.next();
        if (pair.getValue1().equals(identifier)) {
          iterator.remove();
        }
      }
    }
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
              synchronized (productListeners) {
                Activator.debug("received ProductMessage: %s", productMessage);
                List<Pair<Consumer<ProductMessage>, Object>> listeners =
                    productListeners.get(productMessage.getProduct());
                // TODO: check, if source and language of productMessage match the opened file?

                if (listeners == null) {
                  Activator.debug("Ignoring ProductMessage %s, because no listener wants it",
                      productMessage);
                } else {
                  for (Pair<Consumer<ProductMessage>, Object> consumer : listeners) {
                    consumer.getValue0().accept(productMessage);
                  }
                }
              }
            }, discoveryResponse -> {
              synchronized (discoveryListeners) {
                Activator.debug("Received DiscoveryResponse: %s", discoveryResponse);
                for (Pair<Consumer<DiscoveryResponse>, Object> listener : discoveryListeners) {
                  listener.getValue0().accept(discoveryResponse);
                }
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
