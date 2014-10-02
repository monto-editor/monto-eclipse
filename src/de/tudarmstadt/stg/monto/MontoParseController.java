package de.tudarmstadt.stg.monto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.ParseControllerBase;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;

import de.tudarmstadt.stg.monto.client.LineSplitter;
import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.client.Multiplexer;
import de.tudarmstadt.stg.monto.client.ReverseContent;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductEditorInput;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.sink.Sink;
import de.tudarmstadt.stg.monto.sink.SinkViewer;

public class MontoParseController
    extends ParseControllerBase
	implements IParseController {
	
	private MontoClient client;
	private final Set<Product> registeredProducts = new HashSet<>();
	private Source source;

	public MontoParseController() {
		this(new Multiplexer());
		Multiplexer multiplexer = (Multiplexer) client;
		multiplexer.addClient(new ReverseContent());
		multiplexer.addClient(new LineSplitter());
	}
	
	public MontoParseController(MontoClient client) {
		this.client = client;
	}
	
	@Override
	public void initialize(final IPath filePath, final ISourceProject project,
			final IMessageHandler handler) {
		
		super.initialize(filePath, project, handler);

		source = new Source(filePath.toString());
		
		// When a new product message arrives, check if the product is allready registered,
		// else open a new editor window which receives the contents of this product.
		client.addProductMessageListener((productMessage) -> {
			final Product product = productMessage.getProduct();
			
			if(! registeredProducts.contains(product)) {
				
				registeredProducts.add(product);
				final IEditorInput input = new ProductEditorInput(source, product);
				
				Display.getDefault().asyncExec(() -> {
					try {
						final SinkViewer editor = (SinkViewer) PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(input, "de.tudarmstadt.stg.monto.sinkviewer");
				
							
						final Sink sink = (Sink) editor.getDocumentProvider().getDocument(input);
					
						client.addProductMessageListener(sink);
					
						// The sink hasn't recieved the first product message yet.
						sink.onProductMessage(productMessage);
						
					} catch (Exception e) {
						
						// if for whatever reason a exception occurs, the product needs to be deregistered.
						registeredProducts.remove(product);
						Activator.error(e);
					}
				});
			}
		});
	}

	@Override
	public Object parse(String documentText, IProgressMonitor monitor) {
		
		final Source source = new Source(this.getPath().toString());;
		final Language language = LanguageRegistry.findLanguage(getPath(), getDocument());
		final Contents contents = new StringContent(documentText);;
		final Selection selection = null;
		
		client.sendVersionMessage(
				source,
				language,
				contents,
				selection);
		
		return null;
	}
	
	@Override
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISourcePositionLocator getSourcePositionLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILanguageSyntaxProperties getSyntaxProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getTokenIterator(IRegion region) {
		// TODO Auto-generated method stub
		return null;
	}
}
