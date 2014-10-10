package de.tudarmstadt.stg.monto.message;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

public class ProductMessageTest {

	@Test
	public void canBeDecodedFromJSON() throws ProductMessageParseException {
		String json
			= "{\n"
			+ "  \"source\" : \"file.txt\",\n"
			+ "  \"product\" : \"length\",\n"
			+ "  \"language\" : \"number\",\n"
			+ "  \"contents\" : \"18\"\n"
			+ "}";
		ProductMessage message = ProductMessage.decode(new StringReader(json));
		assertEquals("file.txt", message.getSource().toString());
		assertEquals("length",   message.getProduct().toString());
		assertEquals("number",   message.getLanguage().toString());
		assertEquals("18",       message.getContents().string());
	}

}
