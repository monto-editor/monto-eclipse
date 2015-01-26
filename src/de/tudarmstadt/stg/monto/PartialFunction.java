package de.tudarmstadt.stg.monto;

@FunctionalInterface
public interface PartialFunction<A, B, E extends Throwable> {
	public B apply(A a) throws E;
}
