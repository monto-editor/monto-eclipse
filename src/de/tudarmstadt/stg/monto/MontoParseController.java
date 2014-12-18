package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.outline.Outline;
import de.tudarmstadt.stg.monto.outline.Outlines;
import de.tudarmstadt.stg.monto.region.Region;
import de.tudarmstadt.stg.monto.server.ProductMessageListener;

public class MontoParseController extends ParseControllerBase {
	
	private final SourceConnection sourceConnection = Activator.getSourceConnection();
	private final SinkConnection sinkConnection = Activator.getSinkConnection();
	private Source source = null;
	private Language language = null;
	private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
	private UniversalEditor editor;
	private LongKey id = new LongKey(0);
	private WaitOnTokens tokensFuture = null;
	private WaitOnOutline outlineFuture = null;
	private WaitOnCompletions completionsFuture = null;
	
	private synchronized LongKey freshId() {
		id = id.freshId();
		return id;
	}
	
	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		source = new Source(String.format("/%s/%s",project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
	}

	@Override
	public synchronized Object parse(String documentText, IProgressMonitor monitor) {				
		try {
			final LongKey transactionId = freshId();
			final Contents contents = new StringContent(documentText);
			final List<Selection> selections = new ArrayList<>();
			if(editor != null) {
				Display.getDefault().syncExec(() -> {
					IRegion region = editor.getSelectedRegion();
					selections.add(new Selection(region.getOffset(), region.getLength()));
				});
			}
			
			cancleFutures();
			final VersionMessage version = new VersionMessage(transactionId,source,language,contents,selections);
			tokensFuture = new WaitOnTokens(sinkConnection, version);
			outlineFuture = new WaitOnOutline(sinkConnection, version);
			completionsFuture = new WaitOnCompletions(sinkConnection, version);
			Activator.getProfiler().start(MontoParseController.class, "version", version);
			sourceConnection.sendVersionMessage(version);
		} catch (Exception e) {
			Activator.error(e);
		}
		
		return null;
	}
	
	private void cancleFutures() {
		if(tokensFuture != null)
			tokensFuture.cancel(true);
		if(completionsFuture != null)
			completionsFuture.cancel(true);
		if(outlineFuture != null)
			outlineFuture.cancel(true);
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
			List<Token> tokens = tokensFuture.get(50, TimeUnit.MILLISECONDS);
			return tokens.stream()
					.filter((token) -> token.inRange(new Region(region)))
					.iterator();
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Activator.error(e);
			return new ArrayList<>().iterator();
		}
	}
	
	public List<Completion> getCompletions() {
		try {
			return completionsFuture.get(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Activator.error(e);
			return new ArrayList<>();
		}
	}
	
	@Override
	public Object getCurrentAst() {
		try {
			return outlineFuture.get(100,TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}

	private static class WaitOnTokens extends WaitOnProduct<List<Token>> {

		public WaitOnTokens(SinkConnection sinkConnection,	VersionMessage version) {
			super(sinkConnection, version);
		}

		@Override
		protected boolean relevant(ProductMessage message) {
			return message.getProduct().equals(Products.tokens)
			    && message.getLanguage().equals(Languages.json);
		}

		@Override
		protected List<Token> parseProductMessage(ProductMessage message) throws ParseException {
				return Tokens.decode(message.getContents().getReader());
		}
	}
	
	private static class WaitOnCompletions extends WaitOnProduct<List<Completion>> {

		public WaitOnCompletions(SinkConnection sinkConnection,	VersionMessage version) {
			super(sinkConnection, version);
		}

		@Override
		protected boolean relevant(ProductMessage message) {
			return message.getProduct().equals(Products.completions)
			    && message.getLanguage().equals(Languages.json);
		}

		@Override
		protected List<Completion> parseProductMessage(ProductMessage message) throws ParseException {
			return Completions.decode(message.getContents().getReader());
		}
	}
	
	private static class WaitOnOutline extends WaitOnProduct<ParseResult> {

		public WaitOnOutline(SinkConnection sinkConnection,	VersionMessage version) {
			super(sinkConnection, version);
		}

		@Override
		protected boolean relevant(ProductMessage message) {
	        return message.getProduct().equals(Products.outline)
	            && message.getLanguage().equals(Languages.json);
		}

		@Override
		protected ParseResult parseProductMessage(ProductMessage message) throws ParseException {
			Outline outline = Outlines.decode(message.getContents().getReader());
			return new ParseResult(outline,version.getContent().toString());
		}
	}
	
	private static abstract class WaitOnProduct<A> implements Future<A>, ProductMessageListener, AutoCloseable {
		private static enum State {WAITING, DONE, CANCELLED}

		private final BlockingQueue<ProductMessage> reply = new ArrayBlockingQueue<>(1);
		private State state = State.WAITING;
		private SinkConnection sinkConnection;
		protected VersionMessage version;
		private A parsed;
		
		public WaitOnProduct(SinkConnection sinkConnection,	VersionMessage version) {
			this.sinkConnection = sinkConnection;
			this.version = version;
			this.parsed = null;
			sinkConnection.addSink(this);
		}
		
		protected abstract boolean relevant(ProductMessage message);
		protected abstract A parseProductMessage(ProductMessage message) throws ParseException;
				
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			try {
				state = State.CANCELLED;
				close();
				return true; 
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// This thing is so f**ing ugly.
		private A doGet(SupplierException<ProductMessage,InterruptedException> supplier) throws InterruptedException {
			if(isCancelled())
				return null;
			if(parsed == null) {
				final ProductMessage product = supplier.get();
				if(product == null) {
					state = State.CANCELLED;
					return null;
				}
				try {
					parsed = parseProductMessage(product);
					if(parsed == null)
						state = State.CANCELLED; 
				} catch (Exception e) {
					Activator.error(e);
					state = State.CANCELLED;
					return null;
				}
				if(isCancelled())
					return null;
				else return parsed;
			} else {
				return parsed;
			}
		}

		@Override
		public A get() throws InterruptedException, ExecutionException {
			return doGet(() -> reply.take());
		}

		@Override
		public A get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			return doGet(() -> reply.poll(timeout, unit));
		}

		@Override
		public boolean isCancelled() {
			return state == State.CANCELLED;
		}

		@Override
		public boolean isDone() {
			return state == State.DONE;
		}

		@Override
		public void onProductMessage(ProductMessage message) {
			try {
				if(version.getVersionId().equals(message.getVersionId()) && relevant(message)) {
					reply.put(message);
					Activator.getProfiler().end(MontoParseController.class, "product_"+message.getProduct(), message);
					state = State.DONE;
				}
			} catch (InterruptedException e) {}
		}

		@Override
		public void close() throws Exception {
			sinkConnection.removeSink(this);
		}
	}
	
	@FunctionalInterface
	private interface SupplierException<A,E extends Exception> {
		public A get() throws E;
	}
}
