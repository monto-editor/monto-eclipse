package de.tudarmstadt.stg.monto.message;

import org.eclipse.imp.language.Language;

public class VersionMessage {

	private Source source;
	private Contents content;
	private Language language;
	private Selection selection;

	public VersionMessage(Source source, Language language, Contents content, Selection selection) {
		this.source = source;
		this.language = language;
		this.content = content;
		this.selection = selection;
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

	public Selection getSelection() {
		return selection;
	}
}
