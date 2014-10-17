package de.tudarmstadt.stg.monto.message;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringContent implements Contents {
	
	private String content;

	public StringContent(String content) {
		this.content = content;
	}

	@Override
	public InputStream bytes() {
		return new ByteArrayInputStream(content.getBytes());
	}

	@Override
	public String string() {
		return content;
	}
	
	@Override
	public String toString() {
		return content;
	}
}
