package monto.eclipse.demultiplex;

import monto.service.types.LongKey;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class VersionIdBasedProductCache<A> extends ProductCache<A> {
  public VersionIdBasedProductCache(String logProductTag) {
    super(logProductTag);
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

  public void onProductMessage(A product, LongKey versionId) {
    withLock(() -> {
      System.out.printf("state: %s version: %s newVersion: %s", state.toString(), this.versionId, versionId);
      if ((state == Fetch.PENDING || state == Fetch.WAITING) && versionId.upToDate(this.versionId)) {
        this.product = product;
        this.state = Fetch.ARRIVED;
        arrived.signalAll();
      }
    });
  }

  @Override
  @Deprecated
  public void onProductMessage(A product) {
    throw new RuntimeException("use onProductMessage(A product, LongKey versionId)");
  }

  @Override
  @Deprecated
  public void invalidateProduct() {
    throw new RuntimeException("use invalidateProduct(LongKey newVersionID)");
  }

}
