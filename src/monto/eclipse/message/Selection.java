package monto.eclipse.message;

import java.util.Arrays;
import java.util.List;

import monto.eclipse.region.Region;

public class Selection extends Region {
	
	public Selection(int offset, int length) {
		super(offset,length);
	}
	
	public static List<Selection> selections(Selection ... sel) {
		return Arrays.asList(sel);
	}

	@Override
	public String toString() {
		return String.format("offset: %d, length: %d", getStartOffset(), getLength());
	}
}
