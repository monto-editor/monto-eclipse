package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

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
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.LongKey;
import de.tudarmstadt.stg.monto.message.MessageListener;
import de.tudarmstadt.stg.monto.message.ParseException;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Products;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.outline.Outlines;
import de.tudarmstadt.stg.monto.region.Region;

public class MontoParseController extends ParseControllerBase implements MessageListener {

	private Source source = null;
	private Language language = null;
	private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
	private UniversalEditor editor;
	private LongKey id = new LongKey(0);
	private WaitOnMessage tokensFuture = null;
	private WaitOnMessage completionsFuture = null;
	private WaitOnMessage outlineFuture = null;
	
	private synchronized LongKey freshId() {
		id = id.freshId();
		return id;
	}
	
	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		source = new Source(String.format("/%s/%s",project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
		Activator.addMessageListener(this);
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
			
			final VersionMessage version = new VersionMessage(transactionId,source,language,contents,selections);

			cancleFutures();
			startFutures(version);
			
//			Activator.getProfiler().start(MontoParseController.class, "version", version);
			Activator.sendMessage(version);
		} catch (Exception e) {
			Activator.error(e);
		}
		
		return null;
	}
	
	private void startFutures(VersionMessage version) {
		tokensFuture = new WaitOnMessage(version, Products.tokens, Languages.json);
		completionsFuture = new WaitOnMessage(version, Products.completions, Languages.json);
		outlineFuture = new WaitOnMessage(version, Products.outline, Languages.json);
	}

	private void cancleFutures() {
		cancleFuture(tokensFuture);
		cancleFuture(completionsFuture);
		cancleFuture(outlineFuture);
	}
	
	private static void cancleFuture(WaitOnMessage future) {
		if(future != null) {
			future.cancel(true);
		}
	}

	@Override
	public void onMessage(ProductMessage message) {
		notifyFuture(tokensFuture, message);
		notifyFuture(completionsFuture, message);
		notifyFuture(outlineFuture, message);
	}
	
	private void notifyFuture(WaitOnMessage future, ProductMessage message) {
		if(future != null) {
			future.onMessage(message);
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

	private static <T> Function<ProductMessage,Optional<T>> parse(PartialFunction<ProductMessage, T, ParseException> parser) {
		return msg -> {
			try {
				return Optional.of(parser.apply(msg));
			} catch (ParseException e) {
				return Optional.empty();
			}
		};
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getTokenIterator(final IRegion region) {
		try {
			List<Token> tokens = tokensFuture
					.get(50, TimeUnit.MILLISECONDS)
					.flatMap(parse(Tokens::decode))
					.orElse(new ArrayList<Token>());
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
			return completionsFuture.get(100, TimeUnit.MILLISECONDS)
					.flatMap(parse(Completions::decode))
					.orElse(new ArrayList<Completion>());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Activator.error(e);
			return new ArrayList<>();
		}
	}
	
	@Override
	public Object getCurrentAst() {
		try {
			return outlineFuture.get(100,TimeUnit.MILLISECONDS)
					.flatMap(parse(Outlines::decode))
					.map(outline -> new ParseResult(outline, outlineFuture.getVersionMessage().getContent().toString()))
					.orElse(null);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}
	
	private static class WaitOnMessage implements Future<Optional<ProductMessage>>, MessageListener, AutoCloseable {
		private static enum State {WAITING, DONE, CANCELLED}

		private final BlockingQueue<ProductMessage> reply = new ArrayBlockingQueue<>(1);
		private State state = State.WAITING;
		protected VersionMessage version;
		private Product product;
		private Language language;
		
		public WaitOnMessage(VersionMessage version, Product product, Language language) {
			this.version = version;
			this.product = product;
			this.language = language;
		}
				
		public VersionMessage getVersionMessage() {
			return version;
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
		
		private Optional<ProductMessage> doGet(SupplierException<ProductMessage,InterruptedException> supplier) throws InterruptedException {
			if(isCancelled())
				return Optional.empty();
			final ProductMessage message = supplier.get();
			if(message == null) {
				state = State.CANCELLED;
				return Optional.empty();
			}
			if(isCancelled())
				return Optional.empty();
			else return Optional.of(message);
		}

		@Override
		public Optional<ProductMessage> get() throws InterruptedException, ExecutionException {
			return doGet(() -> reply.take());
		}

		@Override
		public Optional<ProductMessage> get(long timeout, TimeUnit unit)
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
		public void onMessage(ProductMessage message) {
			try {
				if(message.getSource().equals(version.getSource())
						&& message.getVersionId().equals(version.getVersionId())
						&& message.getProduct().equals(product)
						&& message.getLanguage().equals(language)
						) {
					reply.put(message);
					state = State.DONE;
				}
			} catch (InterruptedException e) {}
		}

		@Override
		public void close() throws Exception {
		}
		
		@Override
		public int hashCode() {
			return version.hashCode();
		}
		
		@FunctionalInterface
		private interface SupplierException<A,E extends Exception> {
			public A get() throws E;
		}
	}
}
