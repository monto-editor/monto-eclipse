package de.tudarmstadt.stg.monto.message;

import java.io.InputStream;
import java.io.Reader;

public interface Contents {
	public InputStream getBytes();
	public Reader getReader();
}
