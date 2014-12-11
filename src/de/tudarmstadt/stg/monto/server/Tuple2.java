package de.tudarmstadt.stg.monto.server;

public class Tuple2<A,B> {
	private A a;
	private B b;

	public Tuple2(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public int hashCode() {
		return (a.toString()+b.toString()).hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof Tuple2<?,?>) {
			Tuple2<A,B> other = (Tuple2<A, B>) obj;
			return this.a.equals(other.a) && this.b.equals(other.b);
		} else {
			return false;
		}
	}
}
