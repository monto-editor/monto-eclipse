package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.ParseControllerBase;
import org.eclipse.imp.parser.SimpleAnnotationTypeInfo;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;

import de.tudarmstadt.stg.monto.color.Token;
import de.tudarmstadt.stg.monto.color.Tokens;
import de.tudarmstadt.stg.monto.completion.Completion;
import de.tudarmstadt.stg.monto.completion.Completions;
import de.tudarmstadt.stg.monto.connection.SinkConnection;
import de.tudarmstadt.stg.monto.connection.SourceConnection;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;
import de.tudarmstadt.stg.monto.region.Region;
import de.tudarmstadt.stg.monto.sink.Sink;

public class MontoParseController extends ParseControllerBase implements Sink, AutoCloseable {
	
	private final SourceConnection sourceConnection = Activator.getSourceConnection();
	private final SinkConnection sinkConnection = Activator.getSinkConnection();
	private MVar<List<Completion>> completions = new MVar<>(new ArrayList<>());
	private MVar<List<Token>> tokens = new MVar<>();
	private MVar<Outline> outline = new MVar<>();
	private Source source = null;
	private Language language = null;
	private ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
	private UniversalEditor editor;
	
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
		final Contents contents = new StringContent(documentText);
		final List<Selection> selections = new ArrayList<>();
		if(editor != null) {
			Display.getDefault().syncExec(() -> {
				IRegion region = editor.getSelectedRegion();
				selections.add(new Selection(region.getOffset(), region.getLength()));
			});
		}
		
		try {
			sourceConnection.sendVersionMessage(
					source,
					language,
					contents,
					selections);
			
			fCurrentAst = new ParseResult(
					outline.take(100, TimeUnit.MILLISECONDS),
					documentText);
		} catch (Exception e) {
			Activator.error(e);
			return null;
		}
		
		return null;
	}

	@Override
	public void onProductMessage(ProductMessage message) {

		try {
			if(message.getSource().equals(source)) {
				if(message.getProduct().equals(Products.tokens)) {
					tokens.put(Tokens.decode(message.getContents().getReader()));
				} else if(message.getProduct().equals(Products.outline)) {
					outline.put(Outlines.decode(message.getContents().getReader()));
				} else if(message.getProduct().equals(Products.completions)) {
					completions.put(Completions.decode(message.getContents().getReader()));
				}
			}
		} catch (Exception e) {
			Activator.error(e);
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
			return tokens.take(50, TimeUnit.MILLISECONDS)
						 .stream()
						 .filter((token) -> token.inRange(new Region(region)))
						 .iterator();
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<Completion> getCompletions() {
		try {
			return completions.take(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return new ArrayList<>();
		}
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}
}
