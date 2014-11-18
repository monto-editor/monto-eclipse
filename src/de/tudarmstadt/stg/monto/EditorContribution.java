package de.tudarmstadt.stg.monto;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tudarmstadt.stg.monto.connection.SinkConnection;
import de.tudarmstadt.stg.monto.message.ProductEditorInput;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.sink.DocumentSink;
import de.tudarmstadt.stg.monto.sink.SinkDocumentProvider;


public class EditorContribution implements ILanguageActionsContributor {
	
	@Override
	public void contributeToEditorMenu(final UniversalEditor editor, final IMenuManager menuManager) {
		final MenuManager openProduct = new MenuManager("Open Monto Product");
		final Source source = new Source(getPath(editor.getEditorInput()));
	
		SinkConnection sinkConnection = Activator.getSinkConnection();
		sinkConnection.availableProducts(source).forEach((product) -> {
			final IEditorInput input = new ProductEditorInput(source, product);
			
			openProduct.add(new Action(product.toString()) {				
				@Override
				public void run() {
					try {
						final ITextEditor sinkViewer = (ITextEditor) PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(input, "de.tudarmstadt.stg.monto.sinkviewer");
						
						final SinkDocumentProvider sinkDocumentProvider = (SinkDocumentProvider) sinkViewer.getDocumentProvider();
						
						final DocumentSink sink = (DocumentSink) sinkDocumentProvider.getDocument(input);
						sinkConnection.addSink(sink);
						sinkDocumentProvider.addDisconnectListener(() -> sinkConnection.removeSink(sink));
						
					} catch (Exception e) {
						Activator.error(e);
					}
				}
			});
		});
		
		menuManager.appendToGroup("group.open", openProduct);
	}

	private static String getPath(IEditorInput editorInput) {
		try {
			return ((IStorageEditorInput) editorInput).getStorage().getFullPath().toPortableString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menuManager) {
		
	}

	@Override
	public void contributeToStatusLine(UniversalEditor editor, IStatusLineManager statusLineManager) {
		
	}

	@Override
	public void contributeToToolBar(UniversalEditor editor, IToolBarManager toolBarManager) {
		
	}

}
