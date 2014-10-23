package de.tudarmstadt.stg.monto.message;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class StringContent implements Contents {
	
	private String content;

	public StringContent(String content) {
		this.content = content;
	}

	@Override
	public InputStream getBytes() {
		return new ByteArrayInputStream(content.getBytes());
	}
	
	@Override
	public String toString() {
		return content;
	}

	@Override
	public Reader getReader() {
		return new StringReader(toString());
	}
}
