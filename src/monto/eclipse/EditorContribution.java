package monto.eclipse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import monto.eclipse.message.ProductEditorInput;
import monto.eclipse.message.Source;
import monto.eclipse.message.ProductRegistry.ProductItem;
import monto.eclipse.sink.DocumentSink;
import monto.eclipse.sink.SinkDocumentProvider;

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

public class EditorContribution implements ILanguageActionsContributor {
	
	@Override
	public void contributeToEditorMenu(final UniversalEditor editor, final IMenuManager menuManager) {
		final MenuManager openProduct = new MenuManager("Open Monto Product");
		final Source source = new Source(getPath(editor.getEditorInput()));
		
		List<ProductItem> availableProduct = new ArrayList<>(Activator.availableProducts(source));
		availableProduct.sort(
				Comparator.comparing(ProductItem::getProduct)
				          .thenComparing(ProductItem::getLanguage));
		availableProduct.forEach((item) -> {
			final String actionDescription = String.format("%s - %s", item.getProduct().toString(), item.getLanguage().toString());
			final ProductEditorInput input = new ProductEditorInput(source,item.getProduct(),item.getLanguage());
			
			openProduct.add(new Action(actionDescription) {				
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
						Activator.addMessageListener(sink);
						sinkDocumentProvider.addDisconnectListener(() -> Activator.removeMessageListener(sink));
						
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
