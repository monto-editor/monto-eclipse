package de.tudarmstadt.stg.monto.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.stg.monto.PartialFunction;

public class VersionMessage implements Message {

	private final LongKey versionId;
	private final Source source;
	private final Contents content;
	private final Language language;
	private final List<Selection> selections;
	private final List<Dependency> invalid;

	public VersionMessage(LongKey versionId, Source source, Language language, Contents content, Selection ... selections) {
		this(versionId,source,language,content,Arrays.asList(selections));
	}
	
	public VersionMessage(LongKey versionId, Source source, Language language, Contents content, List<Selection> selections) {
		this(versionId,source,language,content,selections,new ArrayList<>());
	}
	
	public VersionMessage(LongKey id,Source source, Language language, Contents content, List<Selection> selections, List<Dependency> invalid) {
		this.versionId = id;
		this.source = source;
		this.language = language;
		this.content = content;
		this.selections = selections;
		this.invalid = invalid;
	}
	
	public LongKey getVersionId() {
		return versionId;
	}

	@Override
	public Source getSource() {
		return source;
	}

	public Contents getContent() {
		return content;
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	public List<Selection> getSelections() {
		return selections;
	}
	
	@Override
	public List<Dependency> getInvalid() {
		return invalid;
	}

	@Override
	public <A, E extends Throwable> A match(
			PartialFunction<VersionMessage, A, E> ver,
			PartialFunction<ProductMessage, A, E> prod) throws E {
		return ver.apply(this);
	}
}
