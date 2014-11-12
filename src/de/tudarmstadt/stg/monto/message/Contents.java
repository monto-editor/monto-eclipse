package de.tudarmstadt.stg.monto.message;

import java.io.InputStream;
import java.io.Reader;

import de.tudarmstadt.stg.monto.region.IRegion;

public interface Contents {
	public InputStream getBytes();
	public Reader getReader();
	public Contents extract(int offset, int length);
	public default Contents extract(IRegion region) {
		return extract(region.getStartOffset(),region.getLength());
	}
}
