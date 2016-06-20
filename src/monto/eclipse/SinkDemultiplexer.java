package monto.eclipse;

import java.util.List;
import java.util.Optional;

import monto.ide.SinkSocket;
import monto.service.completion.Completion;
import monto.service.gson.GsonMonto;
import monto.service.highlighting.Token;
import monto.service.outline.Outline;
import monto.service.product.ProductMessage;
import monto.service.product.Products;
import monto.service.types.Language;
import monto.service.types.LongKey;
import monto.service.types.Source;
import monto.service.error.Error;

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

  private ProductCache<Outline> outlineCache;
  private ProductCache<List<Token>> tokensCache;
  private ProductCache<List<Error>> errorsCache;
  private ProductCache<List<Completion>> completionsCache;

  public SinkDemultiplexer(SinkSocket sink, Source source, Language language,
      ProductCache<Outline> outlineCache, ProductCache<List<Token>> tokensCache,
      ProductCache<List<Error>> errorsCache, ProductCache<List<Completion>> completionsCache) {
    this.sink = sink;

    this.source = source;
    this.language = language;

    this.outlineCache = outlineCache;
    this.tokensCache = tokensCache;
    this.errorsCache = errorsCache;
    this.completionsCache = completionsCache;
  }

  public void invalidateProducts(LongKey newVersionID) {
    outlineCache.invalidateProduct(newVersionID);
    tokensCache.invalidateProduct(newVersionID);
    errorsCache.invalidateProduct(newVersionID);
    completionsCache.invalidateProduct(newVersionID);
  }

  public void start() {
    sink.connect();
    running = true;
    thread = new Thread() {
      @Override
      public void run() {
        try {
          while (running) {
            Optional<ProductMessage> maybeProductMessage =
                sink.receive(productMessage -> Optional.of(productMessage), discoveryResponse -> {
                  System.out.println(
                      "Received DiscoveryResponse, while expecting a ProductMessage. Ignoring");
                  return Optional.empty();
                });

            maybeProductMessage.ifPresent(productMessage -> {
              if (productMessage.getSource().equals(source)
                  && productMessage.getLanguage().equals(language)) {
                if (productMessage.getProduct().equals(Products.OUTLINE)) {
                  outlineCache.onProductMessage(GsonMonto.fromJson(productMessage, Outline.class),
                      productMessage);
                } else if (productMessage.getProduct().equals(Products.TOKENS)) {
                  tokensCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Token[].class), productMessage);
                } else if (productMessage.getProduct().equals(Products.ERRORS)) {
                  errorsCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Error[].class), productMessage);
                } else if (productMessage.getProduct().equals(Products.COMPLETIONS)) {
                  completionsCache.onProductMessage(
                      GsonMonto.fromJsonArray(productMessage, Completion[].class), productMessage);
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
            });
          }
          sink.close();
        } catch (Exception e) {
          Activator.error(e);
        }
      }
    };
    thread.start();
  }

  public void stop() {
    running = false;
  }
}
