package monto.eclipse;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import monto.service.product.ProductMessage;
import monto.service.types.LongKey;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class ProductCache<A> {

  private Lock lock;
  private Condition arrived;
  private Optional<A> product;
  private LongKey versionID;
  private long timeout;
  private Fetch state;

  public ProductCache() {
    this.lock = new ReentrantLock();
    this.arrived = lock.newCondition();
    this.timeout = 100;
    this.product = Optional.empty();
  }

  void invalidateProduct(LongKey newVersionID) {
    withLock(() -> {
      this.product = Optional.empty();
      setState(Fetch.PENDING);
      versionID = newVersionID;
      arrived.signalAll();
    });
  }

  void onProductMessage(A product, ProductMessage message) {
    withLock(() -> {
      if ((state == Fetch.PENDING || state == Fetch.WAITING)
          && message.getId().upToDate(versionID)) {
        this.product = Optional.of(product);
        setState(Fetch.ARRIVED);
        arrived.signalAll();
      }
    });
  }

  public Optional<A> getProduct() {
    if (state == Fetch.PENDING) {
      lock.lock();
      try {

        setState(Fetch.WAITING);

        arrived.await(timeout, TimeUnit.MILLISECONDS);

        if (getState() != Fetch.PENDING && getState() != Fetch.ARRIVED)
          setState(Fetch.LOST);

      } catch (InterruptedException e) {
        Activator.error("service got interupted: %s", e);
      } finally {
        lock.unlock();
      }
    }

    Activator.debug("%s: getProduct() -> %s", getState(), product);

    return product;
  }


  private void withLock(Runnable runnable) {
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  public ProductCache<A> setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  private void setState(Fetch state) {
    this.state = state;
  }

  private Fetch getState() {
    return state;
  }

  private enum Fetch {
    /**
     * Represents a state, where a new product is requested, that is not yet available and no one is
     * waiting on it.
     */
    PENDING,

    /**
     * Represents a state, where a new product is requested, that is not yet available and
     * getProduct got called.
     */
    WAITING,

    /**
     * Represents a state, where a new product has arrived, that is not invalidated yet.
     */
    ARRIVED,

    /**
     * Represents a state, where a new product has been requested, but the deadline was missed.
     */
    LOST;
  }
}
