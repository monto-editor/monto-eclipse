package monto.eclipse.message;

import java.io.InputStream;
import java.io.Reader;

import monto.eclipse.region.IRegion;

public interface Contents {
	public InputStream getBytes();
	public Reader getReader();
	public Contents extract(int offset, int length);
	public default Contents extract(IRegion region) {
		return extract(region.getStartOffset(),region.getLength());
	}
}
