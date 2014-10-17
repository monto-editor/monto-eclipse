package de.tudarmstadt.stg.monto.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class ConnectionTest {

	private Connection createConnection() throws ConnectionParseException {
		// Blessed are pythons raw strings, sigh ...
		String configFile
			= "{\n"
			+ "  \"connection\" : {\n"
			+ "    \"from_source\" : \"tcp://127.0.0.1:5000\",\n"
			+ "    \"to_server\"   : \"tcp://127.0.0.1:5001\",\n"
			+ "    \"from_server\" : \"tcp://127.0.0.1:5002\",\n"
			+ "    \"to_sinks\"    : \"tcp://127.0.0.1:5003\",\n"
			+ "    \"threads\"     : 4\n"
			+ "  },\n"
			+ "  \"servers\": [\n"
			+ "  ]\n"
			+ "}";
				
		return Connection.create(new StringReader(configFile));
	}
	
	@Test
	public void canBeObtainedFromTheMontoJSONConfigFile() throws ConnectionParseException {
		Connection con = createConnection();
		assertEquals("tcp://127.0.0.1:5000", con.getFromSource());
		assertEquals("tcp://127.0.0.1:5003", con.getToSinks());
		assertEquals(4, con.getNumberOfThreads());
	}

	
	@Test
	public void canBeOpenedAndClosedSuccessfully() throws ConnectionParseException, IOException {
		Connection con = createConnection();
		con.open();
		con.close();
	}
}
