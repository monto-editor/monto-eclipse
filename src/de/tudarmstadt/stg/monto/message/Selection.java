package de.tudarmstadt.stg.monto.message;

import java.util.Arrays;
import java.util.List;

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
	
	public static List<Selection> selections(Selection ... sel) {
		return Arrays.asList(sel);
	}
}
