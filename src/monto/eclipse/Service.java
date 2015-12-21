package monto.eclipse;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import monto.eclipse.connection.Sink;
import monto.service.product.ProductMessage;
import monto.service.types.Language;
import monto.service.types.LongKey;
import monto.service.types.Product;
import monto.service.types.Source;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI
 * thread that calls {@link #getProduct() getProduct}.
 */
public class Service<A> {

	private Lock lock;
	private Condition arrived;
	private Optional<A> product;
	private Function<ProductMessage, Optional<A>> parser;
	private Sink sink;
	private LongKey versionID;
	private Thread thread;
	private boolean running;
	private long timeout;
	private Fetch state;
	private String subscription;

	private Service(String subscription, Function<ProductMessage, Optional<A>> parser) {
		this.sink = Activator.sink(this.subscription = subscription);
		this.lock = new ReentrantLock();
		this.arrived = lock.newCondition();
		this.parser = parser;
		this.timeout = 100;
		setProduct(Optional.empty());
	}
	
	public Service(Source source, Product product, Function<ProductMessage, Optional<A>> parser) {
		this(String.format("%s %s", source, product), parser);
	}
	
	public Service(Source source, Product product, Language language, Function<ProductMessage, Optional<A>> parser) {
		this(String.format("%s %s %s", source, product, language), parser);
	}
	
	public Service(Source source, Product product, Language language, String serviceID, Function<ProductMessage, Optional<A>> parser) {
		this(String.format("%s %s %s %s", source, product, language, serviceID), parser);
	}
	
	public void invalidateProduct(LongKey newVersionID) {
		withLock( () -> {
			setProduct(Optional.empty());
			setState(Fetch.PENDING);
			versionID = newVersionID;
			arrived.signalAll();
		});
	}
	
	public void start() {
		sink.connect();
		running = true;
		thread = new Thread() {
			@Override public void run() {
				try {
					while(running) {
						Optional<ProductMessage> message = sink.receiveMessage();
						withLock(() -> {
							if((state == Fetch.PENDING || state == Fetch.WAITING) && message.map(msg -> msg.getVersionId().upToDate(versionID)).orElse(false)) {
								setProduct(message.flatMap(msg -> parser.apply(msg)));
								product.ifPresent(p -> setState(Fetch.ARRIVED));
								arrived.signalAll();
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
	
	private void setProduct(Optional<A> product) {
		this.product = product;
	}

	public Optional<A> getProduct() {
		if(state == Fetch.PENDING) {
			lock.lock();
			try {

				setState(Fetch.WAITING);
					
				arrived.await(timeout, TimeUnit.MILLISECONDS);
				
				if(getState() != Fetch.PENDING && getState() != Fetch.ARRIVED)
					setState(Fetch.LOST);

			} catch (InterruptedException e) {
				Activator.error("service got interupted: %s", e);
			} finally {
				lock.unlock();
			}
		}
		
		Activator.debug("%s, %s: getProduct() -> %s", subscription, getState(), product);
		
		return product;
	}
	

	public void withLock(Runnable runnable) {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}

	public Service<A> setTimeout(long timeout) {
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
		 * Represents a state, where a new product is requested, that is not
		 * yet available and no one is waiting on it.
		 */
		PENDING,
		
		/**
		 * Represents a state, where a new product is requested, that is not
		 * yet available and getProduct got called.  
		 */
		WAITING,
		
		/**
		 * Represents a state, where a new product has arrived, that is not
		 * invalidated yet.
		 */
		ARRIVED,
		
		/**
		 * Represents a state, where a new product has been requested, but
		 * the deadline was missed.
		 */
		LOST;
	}
}
