package de.tudarmstadt.stg.monto.message;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class VersionMessageTest {

	@Test
	public void canBeEncodedToJSON() {
		
		Source source = new Source("file.txt");
		Language language = new Language("text");
		Contents contents = new StringContent("Hello World");
		List<Selection> selections = Selection.selections(new Selection(3,5));
		VersionMessage message = new VersionMessage(source,language,contents,selections);
		
		String decoded = VersionMessage.encode(message).toJSONString();
		assertThat("source", decoded, containsString("\"source\":\"file.txt\""));
		assertThat("language", decoded, containsString("\"language\":\"text\""));
		assertThat("contents", decoded, containsString("\"contents\":\"Hello World\""));
		assertThat("selections", decoded, containsString("\"selections\":[{"));
		assertThat("selection beginnig", decoded, containsString("\"begin\":3"));
		assertThat("selection ending", decoded, containsString("\"end\":5"));
	}

}
