package monto.eclipse;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import monto.eclipse.connection.Sink;
import monto.service.message.Language;
import monto.service.message.LongKey;
import monto.service.message.ProductMessage;
import monto.service.message.Source;

public class Service<A> {
	private Lock lock;
	private Condition arrived;
	private Optional<A> product;
	private Function<ProductMessage, Optional<A>> parser;
	private Sink sink;
	private LongKey versionID;
	private Thread thread;
	private String subscription;
	private boolean running;

	public Service(Source source, Language language, String product, Function<ProductMessage, Optional<A>> parser) {
		String capProduct = product.substring(0,1).toUpperCase() + product.substring(1);
		subscription = String.format("%s %s%s", source, language, capProduct);
		this.sink = Activator.sink(subscription);
		this.lock = new ReentrantLock();
		this.arrived = lock.newCondition();
		this.product = Optional.empty();
		this.parser = parser;
	}
	
	public void invalidateProduct(LongKey newVersionID) {
		withLock( () -> {
			product = Optional.empty();
			arrived.signalAll();
			versionID = newVersionID;
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
						withLock( () -> {
							product = message
									.map(msg -> {
										return msg;
									})
									.filter(msg -> msg.getVersionId().upToDate(versionID))
									.flatMap(msg -> parser.apply(msg));
							arrived.signalAll();
						});
					}
					sink.close();
					Activator.debug("service %s shutdown correctly", subscription);
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
	
	public Optional<A> getProduct() {
		return product.map(Optional::of).orElseGet(() -> {
			lock.lock();
			try {
				arrived.await(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// Ignore interruption
			} finally {
				lock.unlock();
			}
			return product;
		});
	}
	
	public void withLock(Runnable runnable) {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}
}
