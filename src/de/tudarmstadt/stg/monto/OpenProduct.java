package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.ProductEditorInput;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.sink.Sink;
import de.tudarmstadt.stg.monto.sink.SinkDocumentProvider;



public class OpenProduct implements ILanguageActionsContributor {

	private final MontoClient client;

	
	public OpenProduct() {
		this(Activator.getDefault().getMontoClient());
	}
	
	public OpenProduct(MontoClient client) {
		this.client = client;
	}
	
	@Override
	public void contributeToEditorMenu(final UniversalEditor editor, final IMenuManager menuManager) {
		final MenuManager openProduct = new MenuManager("Open Monto Product");
		
		final Source source = new Source(editor.getEditorInput().getName());
		final org.eclipse.imp.language.Language impLanguage = org.eclipse.imp.language.LanguageRegistry.findLanguage(editor.getEditorInput(), editor.getDocumentProvider());
		final Language language = new Language(impLanguage.toString());
		
		client.availableProducts(source, language).forEach((product) -> {
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
						
						final Sink sink = (Sink) sinkDocumentProvider.getDocument(input);
						client.addProductMessageListener(sink);
						sinkDocumentProvider.addDisconnectListener(() -> client.removeProductMessageListener(sink));
						
						final Contents contents = new StringContent(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());
						final List<Selection> selections = new ArrayList<>();
						client.sendVersionMessage(source, language, contents, selections);
						
					} catch (PartInitException e) {
						Activator.error(e);
					}
				}
			});
		});
		
		menuManager.appendToGroup("group.open", openProduct);
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
