package monto.eclipse.demultiplex;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import monto.eclipse.Activator;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class ProductCache<A> {
  protected Lock lock;
  protected Condition arrived;
  protected Optional<A> product;
  protected long timeout;
  protected Fetch state;

  public ProductCache() {
    this.lock = new ReentrantLock();
    this.arrived = lock.newCondition();
    this.timeout = 100;
    this.product = Optional.empty();
  }

  protected void invalidateProduct() {
    withLock(() -> {
      this.product = Optional.empty();
      setState(Fetch.PENDING);
      arrived.signalAll();
    });
  }

  protected void onProductMessage(A product) {
    withLock(() -> {
      if ((state == Fetch.PENDING || state == Fetch.WAITING)) {
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


  protected void withLock(Runnable runnable) {
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  protected void setState(Fetch state) {
    this.state = state;
  }

  protected Fetch getState() {
    return state;
  }

  protected enum Fetch {
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
