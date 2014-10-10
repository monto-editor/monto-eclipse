package de.tudarmstadt.stg.monto.message;

public class Selection {
	private int begin;
	private int end;
	
	public Selection(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}
}
