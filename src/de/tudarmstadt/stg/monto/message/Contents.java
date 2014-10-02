package de.tudarmstadt.stg.monto.message;

import java.io.InputStream;

public interface Contents {
	public InputStream bytes();
	public String string();
}
