package monto.eclipse.sink;

import java.util.ArrayList;
import java.util.List;

import monto.eclipse.message.ProductEditorInput;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

public class SinkDocumentProvider extends AbstractDocumentProvider {

	private List<DisconnectListener> disconnectListeners = new ArrayList<>();

	public SinkDocumentProvider addDisconnectListener(DisconnectListener listener) {
		disconnectListeners.add(listener);
		return this;
	}
	
	public SinkDocumentProvider removeDisconnectListener(DisconnectListener listener) {
		disconnectListeners.remove(listener);
		return this;
	}
	
	@Override
	protected void disconnected() {
		disconnectListeners.forEach((listener) -> listener.onDisconnect());
		super.disconnected();
	}
	
	@Override
	protected IDocument createDocument(Object obj) throws CoreException {
		final ProductEditorInput input = (ProductEditorInput) obj;
		return new DocumentSink(input.getSource(), input.getProduct(), input.getLanguage());
	}

	@Override
	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {
		return new AnnotationModel();
	}

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
	}

	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}

}
