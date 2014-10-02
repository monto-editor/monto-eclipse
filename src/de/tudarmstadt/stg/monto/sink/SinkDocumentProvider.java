package de.tudarmstadt.stg.monto.sink;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

import de.tudarmstadt.stg.monto.message.ProductEditorInput;

public class SinkDocumentProvider extends AbstractDocumentProvider {

	@Override
	protected IDocument createDocument(Object obj) throws CoreException {
		final ProductEditorInput input = (ProductEditorInput) obj;
		return new Sink(input.getSource(), input.getProduct());
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
