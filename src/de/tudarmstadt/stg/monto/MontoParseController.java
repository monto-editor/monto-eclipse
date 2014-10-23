package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.ParseControllerBase;
import org.eclipse.imp.parser.SimpleAnnotationTypeInfo;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;

import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.ProductMessageListener;
import de.tudarmstadt.stg.monto.message.ProductMessageParseException;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.token.Token;
import de.tudarmstadt.stg.monto.token.TokenMessage;

public class MontoParseController extends ParseControllerBase implements ProductMessageListener, AutoCloseable {
	
	private final MontoClient client;
	private List<Token> tokens;
	private Source source;
	private Language language;
	private ISourcePositionLocator sourcePositionLocator;
	
	public MontoParseController() {
		this(Activator.getDefault().getMontoClient());
	}
	
	public MontoParseController(MontoClient client) {
		this.client = client;
		this.tokens = new ArrayList<>();
	}

	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		client.addProductMessageListener(this);
		source = new Source(String.format("/%s/%s",project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
		sourcePositionLocator = new SourcePositionLocator();
	}

	@Override
	public void close() throws Exception {
		client.removeProductMessageListener(this);
	}
	
	@Override
	public Object parse(String documentText, IProgressMonitor monitor) {
		final Contents contents = new StringContent(documentText);;
		final List<Selection> selections = new ArrayList<>();
		
		client.sendVersionMessage(
				source,
				language,
				contents,
				selections);
		
		return null;
	}

	@Override
	public void onProductMessage(ProductMessage message) {

		if(message.getSource().equals(source)
		&& message.getLanguage().equals(language)
		&& message.getProduct().toString().equals("tokens")) {
			try {
				tokens = TokenMessage.decode(message);
			} catch (ProductMessageParseException e) {
				Activator.error(e);
			}
		}
	}
	
	@Override
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return new SimpleAnnotationTypeInfo();
	}

	@Override
	public ISourcePositionLocator getSourcePositionLocator() {
		return sourcePositionLocator;
	}

	@Override
	public ILanguageSyntaxProperties getSyntaxProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getTokenIterator(final IRegion region) {
		return tokens.stream().filter((token) -> token.inRange(region)).iterator();
	}

}
