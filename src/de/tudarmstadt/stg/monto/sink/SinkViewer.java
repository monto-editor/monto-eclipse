package de.tudarmstadt.stg.monto.sink;

import org.eclipse.ui.editors.text.TextEditor;

public class SinkViewer extends TextEditor {
	
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
