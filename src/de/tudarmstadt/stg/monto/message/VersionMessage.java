package de.tudarmstadt.stg.monto.message;

import java.util.Arrays;
import java.util.List;

public class VersionMessage implements Message {

	private LongKey id;
	private Source source;
	private Contents content;
	private Language language;
	private List<Selection> selections;

	public VersionMessage(LongKey id, Source source, Language language, Contents content, Selection ... selections) {
		this(id,source,language,content,Arrays.asList(selections));
	}
	
	public VersionMessage(LongKey id,Source source, Language language, Contents content, List<Selection> selections) {
		this.id = id;
		this.source = source;
		this.language = language;
		this.content = content;
		this.selections = selections;
	}
	
	public LongKey getId() {
		return id;
	}

	public Source getSource() {
		return source;
	}

	public Contents getContent() {
		return content;
	}

	public Language getLanguage() {
		return language;
	}

	public List<Selection> getSelections() {
		return selections;
	}
}
