package de.tudarmstadt.stg.monto.sink;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class SinkViewer extends AbstractTextEditor {
	public SinkViewer() {
		setDocumentProvider(new SinkDocumentProvider());
	}

}
