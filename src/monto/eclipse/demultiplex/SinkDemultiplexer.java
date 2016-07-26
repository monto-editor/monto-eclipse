package monto.eclipse.demultiplex;

import java.util.List;

import monto.eclipse.Activator;
import monto.ide.SinkSocket;
import monto.service.completion.Completion;
import monto.service.discovery.DiscoveryResponse;
import monto.service.error.Error;
import monto.service.gson.GsonMonto;
import monto.service.highlighting.Token;
import monto.service.outline.Outline;
import monto.service.product.Products;
import monto.service.types.Language;
import monto.service.types.Source;

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

  private Source source;
  private Language language;

  private VersionIdBasedProductCache<Outline> outlineCache;
  private VersionIdBasedProductCache<List<Token>> tokensCache;
  private VersionIdBasedProductCache<List<Error>> errorsCache;
  private VersionIdBasedProductCache<List<Completion>> completionsCache;
  private ProductCache<DiscoveryResponse> discoveryCache;

  public SinkDemultiplexer(SinkSocket sink) {
    this.sink = sink;
  }

  public SinkDemultiplexer setTarget(Source source, Language language) {
    this.source = source;
    this.language = language;

    return this;
  }

  public SinkDemultiplexer setProductCaches(VersionIdBasedProductCache<Outline> outlineCache,
      VersionIdBasedProductCache<List<Token>> tokensCache,
      VersionIdBasedProductCache<List<Error>> errorsCache,
      VersionIdBasedProductCache<List<Completion>> completionsCache) {
    this.outlineCache = outlineCache;
    this.tokensCache = tokensCache;
    this.errorsCache = errorsCache;
    this.completionsCache = completionsCache;

    return this;
  }

  public SinkDemultiplexer setDiscoveryCache(ProductCache<DiscoveryResponse> discoveryCache) {
    this.discoveryCache = discoveryCache;

    return this;
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
              if (productMessage.getSource().equals(source)
                  && productMessage.getLanguage().equals(language)) {
                if (productMessage.getProduct().equals(Products.OUTLINE)) {
                  outlineCache.onProductMessage(GsonMonto.fromJson(productMessage, Outline.class),
                      productMessage.getId());
                } else if (productMessage.getProduct().equals(Products.TOKENS)) {
                  tokensCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Token[].class),
                      productMessage.getId());
                } else if (productMessage.getProduct().equals(Products.ERRORS)) {
                  errorsCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Error[].class),
                      productMessage.getId());
                } else if (productMessage.getProduct().equals(Products.COMPLETIONS)) {
                  completionsCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Completion[].class),
                      productMessage.getId());
                } else {
                  Activator.info(String.format(
                      "Ignoring a unexpected ProductMessage of type %s from service %s",
                      productMessage.getProduct(), productMessage.getServiceId()));
                }
              } else {
                Activator.info(String.format(
                    "Ignoring a ProductMessage of unexpected source %s or language %s",
                    productMessage.getSource(), productMessage.getLanguage()));
              }
            }, discoveryResponse -> {
              Activator.debug("Received DiscoveryResponse: %s", discoveryResponse);
              discoveryCache.onProductMessage(discoveryResponse);
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
