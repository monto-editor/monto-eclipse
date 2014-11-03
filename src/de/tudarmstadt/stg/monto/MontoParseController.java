package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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

import de.tudarmstadt.stg.monto.color.Token;
import de.tudarmstadt.stg.monto.color.Tokens;
import de.tudarmstadt.stg.monto.connection.SinkConnection;
import de.tudarmstadt.stg.monto.connection.SourceConnection;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;
import de.tudarmstadt.stg.monto.sink.Sink;

public class MontoParseController extends ParseControllerBase implements Sink, AutoCloseable {
	
	private final SourceConnection sourceConnection = Activator.getSourceConnection();
	private final SinkConnection sinkConnection = Activator.getSinkConnection();
	private BlockingQueue<List<Token>> tokens = new ArrayBlockingQueue<>(1);;
	private BlockingQueue<Outline> outline = new ArrayBlockingQueue<>(1);;
	private Source source;
	private Language language;
	private ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();;

	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		sinkConnection.addSink(this);
		source = new Source(String.format("/%s/%s",project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
	}

	@Override
	public void close() throws Exception {
		sinkConnection.removeSink(this);
	}
	
	@Override
	public Object parse(String documentText, IProgressMonitor monitor) {
		final Contents contents = new StringContent(documentText);;
		final List<Selection> selections = new ArrayList<>();
		
		tokens.clear();
		outline.clear();
		try {
			sourceConnection.sendVersionMessage(
					source,
					language,
					contents,
					selections);
			
			fCurrentAst = new ParseResult(outline.poll(100, TimeUnit.MILLISECONDS), documentText);
		} catch (Exception e) {
			Activator.error(e);
			return null;
		}
		
		return null;
	}

	@Override
	public void onProductMessage(ProductMessage message) {

		if(message.getSource().equals(source)
		&& message.getLanguage().equals(language)) {
			
			if(message.getProduct().toString().equals("tokens")) {
				try {
					tokens.clear();
					tokens.put(Tokens.decode(message.getContents().getReader()));
				} catch (Exception e) {
					Activator.error(e);
				}
			} else if(message.getProduct().toString().equals("outline")) {
				try {
					outline.clear();
					outline.put(Outlines.decode(message.getContents().getReader()));
				} catch (Exception e) {
					Activator.error(e);
				}
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
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getTokenIterator(final IRegion region) {
		try {
			
			// Wait for 50 milliseconds on the tokenization product.
			// If the product doesn't arrive in time this code throws
			// an exception and returns null.
			return tokens.poll(50, TimeUnit.MILLISECONDS)
						 .stream()
						 .filter((token) -> token.inRange(region))
						 .iterator();
		} catch (Exception e) {
			return null;
		}
	}

}
