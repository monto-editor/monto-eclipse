package de.tudarmstadt.stg.monto.server;

public class Tuple3<A, B, C> {
	private A a;
	private B b;
	private C c;

	public Tuple3(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public int hashCode() {
		return (a.toString()+b.toString()+c.toString()).hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.hashCode() == this.hashCode() && obj instanceof Tuple3<?,?,?>) {
			Tuple3<A,B,C> other = (Tuple3<A, B, C>) obj;
			return this.a.equals(other.a) && this.b.equals(other.b) && this.c.equals(other.c);
		} else {
			return false;
		}
	}
}
