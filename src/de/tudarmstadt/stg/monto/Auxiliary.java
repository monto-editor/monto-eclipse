package de.tudarmstadt.stg.monto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Auxiliary {
	
	public static InputStream emptyInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}
}
