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
import java.util.function.Predicate;

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
	
	private List<Token> tokens = null;
	private Outline outline = null;
	private List<Completion> completions = null;
	private WaitOnProduct tokensFuture = null;
	private WaitOnProduct outlineFuture = null;
	private WaitOnProduct completionsFuture = null;
	private Object tokensLock = new Object();
	private Object outlineLock = new Object();
	private Object completionsLock = new Object();
	
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
		VersionMessage version = new VersionMessage(transactionId,source,language,contents,selections);
		synchronized(tokensLock) {
			tokensFuture = new WaitOnProduct(sinkConnection, version,
					message -> message.getId().equals(transactionId)
					        && message.getProduct().equals(Products.tokens)
					        && message.getLanguage().equals(Languages.json));
		}
		synchronized(outlineLock) {
			outlineFuture = new WaitOnProduct(sinkConnection, version,
					message -> message.getId().equals(transactionId)
					        && message.getProduct().equals(Products.outline)
					        && message.getLanguage().equals(Languages.json));
		}
		synchronized(completionsLock) {
			completionsFuture = new WaitOnProduct(sinkConnection, version,
					message -> message.getId().equals(transactionId)
					        && message.getProduct().equals(Products.completions)
					        && message.getLanguage().equals(Languages.json));
		}
		
		try {
			VersionMessage message = new VersionMessage(transactionId,source,language,contents,selections);
			Activator.getProfiler().start(MontoParseController.class, "version", message);
			sourceConnection.sendVersionMessage(message);
		} catch (Exception e) {
			Activator.error(e);
		}
		return null;
	}
	
	private void cancleFutures() {
		synchronized(tokensLock) {
			if(tokensFuture != null)
				tokensFuture.cancel(true);
			tokens = null;
		}
		synchronized(completionsLock) {
			if(completionsFuture != null)
				completionsFuture.cancel(true);
			completions = null;
		}
		synchronized(outlineLock) {
			if(outlineFuture != null)
				outlineFuture.cancel(true);
			outline = null;
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

		synchronized(tokensLock) {
			if(tokens == null) {
				try {
					ProductMessage message = tokensFuture.get(50, TimeUnit.MILLISECONDS);
					if(message == null)
						return null;
					else
						tokens = Tokens.decode(message.getContents().getReader());
				} catch (Exception e) {
					Activator.error(e);
					return null;
				}
			}
	
			Iterator iterator = tokens.stream()
				.filter((token) -> token.inRange(new Region(region)))
				.iterator();
			return iterator;
		}
	}
	
	public List<Completion> getCompletions() {
		
		synchronized(completionsLock) {
		
			if(completions == null) {
				try {
					ProductMessage message = completionsFuture.get(100, TimeUnit.MILLISECONDS);
					if(message == null)
						return new ArrayList<>();
					else
						completions = Completions.decode(message.getContents().getReader());
	
				} catch (Exception e) {
					Activator.error(e);
					return new ArrayList<>();
				}
			}

			return completions;
		}
	}
	
	@Override
	public Object getCurrentAst() {
		
		synchronized(outlineLock) {
		
			if(outline == null) {
				try {
					ProductMessage message = outlineFuture.get(100, TimeUnit.MILLISECONDS);
					if(message == null)
						return null;
					else
						outline = Outlines.decode(message.getContents().getReader());
	
				} catch (Exception e) {
					Activator.error(e);
					return null;
				}
			}

			return new ParseResult(outline,outlineFuture.getVersionMessage().getContent().toString());
		}
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}
	
	private static class WaitOnProduct implements Future<ProductMessage>, ProductMessageListener, AutoCloseable {
		private static enum State {WAITING, DONE, CANCELLED}

		private final BlockingQueue<ProductMessage> reply = new ArrayBlockingQueue<>(1);
		private final Predicate<ProductMessage> relevant;
		private State state = State.WAITING;
		private SinkConnection sinkConnection;
		private VersionMessage version;
		
		public WaitOnProduct(
				SinkConnection sinkConnection,
				VersionMessage version,
				Predicate<ProductMessage> relevant) {
			this.sinkConnection = sinkConnection;
			this.version = version;
			this.relevant = relevant;
			sinkConnection.addSink(this);
		}
				
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

		@Override
		public ProductMessage get() throws InterruptedException, ExecutionException {
			final ProductMessage product = reply.take();
			if(isCancelled())
				return null;
			else
				return product;
		}

		@Override
		public ProductMessage get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			final ProductMessage product = reply.poll(timeout, unit);
			if(isCancelled())
				return null;
			else
				return product;
		}
		
		public VersionMessage getVersionMessage() {
			return version;
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
				if(relevant.test(message)) {
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
}
