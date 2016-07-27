package monto.eclipse.demultiplex;

import java.util.function.Function;

import monto.service.product.ProductMessage;
import monto.service.types.LongKey;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class VersionIdBasedProductCache<P> extends ProductCache<ProductMessage, P> {
  public VersionIdBasedProductCache(String logProductTag, Function<ProductMessage, P> productMessageDeserializer) {
    super(logProductTag, productMessageDeserializer);
  }

  protected LongKey versionId;

  public void invalidateProduct(LongKey newVersionID) {
    withLock(() -> {
      super.product = null;
      this.state = Fetch.PENDING;
      versionId = newVersionID;
      arrived.signalAll();
    });
  }

  @Override
  public void onProductMessage(ProductMessage productMessage) {
    LongKey newVersionId = productMessage.getId();
    P newProduct = messageDeserializer.apply(productMessage);
    withLock(() -> {
      System.out.printf("state: %s version: %s newVersion: %s", state.toString(), this.versionId, newVersionId);
      if ((state == Fetch.PENDING || state == Fetch.WAITING) && newVersionId.upToDate(this.versionId)) {
        this.product = newProduct;
        this.state = Fetch.ARRIVED;
        arrived.signalAll();
      }
    });
  }

  @Override
  @Deprecated
  public void invalidateProduct() {
    throw new RuntimeException("use invalidateProduct(LongKey newVersionID)");
  }

}
