package monto.eclipse;

import java.util.Optional;

import monto.service.types.LongKey;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class VersionIdBasedProductCache<A> extends ProductCache<A> {
  protected LongKey versionID;

  protected void invalidateProduct(LongKey newVersionID) {
    withLock(() -> {
      super.product = Optional.empty();
      setState(Fetch.PENDING);
      versionID = newVersionID;
      arrived.signalAll();
    });
  }

  protected void onProductMessage(A product, LongKey versionId) {
    withLock(() -> {
      if ((state == Fetch.PENDING || state == Fetch.WAITING) && versionId.upToDate(versionID)) {
        this.product = Optional.of(product);
        setState(Fetch.ARRIVED);
        arrived.signalAll();
      }
    });
  }

  @Override
  @Deprecated
  protected void onProductMessage(A product) {
    throw new RuntimeException("use onProductMessage(A product, LongKey versionId)");
  }

  @Override
  @Deprecated
  protected void invalidateProduct() {
    throw new RuntimeException("use invalidateProduct(LongKey newVersionID)");
  }


}
