package de.tudarmstadt.stg.monto.sink;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class SinkViewer extends AbstractTextEditor {
	public SinkViewer() {
		setDocumentProvider(new SinkDocumentProvider());
	}

	@Override
	public boolean isEditable() {
		return false;
	}
	

	@Override
	public boolean isEditorInputModifiable() {
	    return false;
	}
	
	@Override
	public boolean isEditorInputReadOnly() {
		return true;
	}
	
	@Override
	public boolean isDirty() {
	    return false;
	}
}
