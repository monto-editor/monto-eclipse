package de.tudarmstadt.stg.monto.sink;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Test;

import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductEditorInput;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;

public class SinkTest {
	
	@Test
	public void testSinkUpdatesOnNewProductMessages() throws PartInitException {
		
		Source source = new Source("TestSource");
		Product product = new Product("TestProduct");
		IEditorInput input = new ProductEditorInput(source, product);
		
		SinkViewer editor = openInEditor(input);
		DocumentSink sink = (DocumentSink) editor.getDocumentProvider().getDocument(input);
		
		// Change the contents of the sink synchrounously, 
		// s.t. the contents of the sink can be queried directly
		// after the arrival of the product message.
		sink.setExecutor(Display.getDefault()::syncExec);
		sink.onProductMessage(productMessage(source, product, "Hello World"));
		
		assertEditorContains("Hello World", editor);
		
		sink.onProductMessage(productMessage(source, product, "foobar"));
		assertEditorContains("foobar", editor);
	}
	
	private void assertEditorContains(String expected, IEditorPart editor) {
		ITextEditor e = (ITextEditor) editor;
		IDocument doc = e.getDocumentProvider().getDocument(e.getEditorInput());
		assertEquals("Editor didn't not contain the expected content", expected, doc.get());
	}
	
	private ProductMessage productMessage(Source source, Product product, String msg) {
		Language language = new Language("Monto");
		return new ProductMessage(source, product, language, new StringContent(msg));
	}
	
	private SinkViewer openInEditor(IEditorInput input) throws PartInitException {
		
		return (SinkViewer) PlatformUI
			.getWorkbench()
			.getActiveWorkbenchWindow()
			.getActivePage()
			.openEditor(input, "de.tudarmstadt.stg.monto.sinkviewer");
	}
}
