package de.tudarmstadt.stg.monto;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MVar<A> {
	private BlockingQueue<A> queue = new ArrayBlockingQueue<>(1);
	A value = null;
	
	public MVar(A a) {
		queue.add(a);
		this.value = a;
	}
	
	public MVar() {}
	
	public void put(A a) throws InterruptedException {
		queue.clear();
		queue.put(a);
	}
	
	public A take(long time, TimeUnit unit) throws InterruptedException {		
		A value = queue.poll(time,unit);
		this.value = null;
		return value;
	}
	
	public A get(long time, TimeUnit unit) throws InterruptedException {
		A value = queue.poll(time,unit);
		if(value == null) {
			return this.value;
		} else {
			return this.value = value;
		}
	}
}
