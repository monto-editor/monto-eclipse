package monto.eclipse;

import static monto.eclipse.OptionalUtils.withException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.editor.quickfix.IAnnotation;
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

import monto.service.completion.Completion;
import monto.service.gson.GsonMonto;
import monto.service.outline.Outline;
import monto.service.product.Products;
import monto.service.region.Region;
import monto.service.source.SourceMessage;
import monto.service.token.Token;
import monto.service.types.Language;
import monto.service.types.LongKey;
import monto.service.types.Selection;
import monto.service.types.Source;

public class MontoParseController extends ParseControllerBase {

	private String contents = null;
	private Source source = null;
	private Language language = null;
	private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
	private UniversalEditor editor;

	private LongKey versionID = new LongKey(0);
	private Service<Outline> outline;
	private Service<List<Token>> tokens;
	private Service<List<Completion>> completions;
	private Service<List<monto.service.error.Error>> errors;
	private List<Service<?>> services = new ArrayList<>();

	private static final Map<String, Object> errorSeverity = new HashMap<>();
	private static final Map<String, Object> warningSeverity = new HashMap<>();
	private static final Map<String, Object> infoSeverity = new HashMap<>();

	static {
		errorSeverity.put(IMessageHandler.SEVERITY_KEY, IAnnotation.ERROR);
		warningSeverity.put(IMessageHandler.SEVERITY_KEY, IAnnotation.WARNING);
		infoSeverity.put(IMessageHandler.SEVERITY_KEY, IAnnotation.INFO);
	}

	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		source = new Source(String.format("/%s/%s", project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
		services.add(completions = new Service<List<Completion>>(source, Products.COMPLETIONS, language,
				withException(msg -> GsonMonto.fromJsonArray(msg, Completion[].class))).setTimeout(500));
		services.add(tokens = new Service<List<Token>>(source, Products.TOKENS, language,
				withException(msg -> GsonMonto.fromJsonArray(msg, Token[].class))));
		services.add(outline = new Service<Outline>(source, Products.OUTLINE, language,
				withException(msg -> GsonMonto.fromJson(msg, Outline.class))).setTimeout(500));
		services.add(errors = new Service<List<monto.service.error.Error>>(source, Products.ERRORS, language,
				withException(msg -> GsonMonto.fromJsonArray(msg, monto.service.error.Error[].class))).setTimeout(500));
		startServices();
	}

	@Override
	public void dispose() {
		stopServices();
	}

	private void startServices() {
		services.forEach(service -> service.start());
	}

	private void stopServices() {
		services.forEach(service -> service.stop());
	}

	@Override
	public Object parse(String documentText, IProgressMonitor monitor) {
		try {
			contents = documentText;
			final List<Selection> selections = new ArrayList<>();
			if (editor != null) {
				Display.getDefault().syncExec(() -> {
					IRegion region = editor.getSelectedRegion();
					selections.add(new Selection(region.getOffset(), region.getLength()));
				});
			}

			Selection selection = selections.size() >= 1 ? selections.get(0) : null;

			versionID.increment();
			services.forEach(service -> service.invalidateProduct(versionID));
			SourceMessage version = new SourceMessage(versionID, source, language, contents, selection);
			Activator.sendMessage(version);
		} catch (Exception e) {
			e.printStackTrace();
			Activator.error(e);
		}

		return null;
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
		return tokens
			.getProduct()
			.orElse(new ArrayList<>())
			.stream()
			.filter((token) -> token.inRange(new Region(region.getOffset(), region.getLength())))
			.iterator();
	}

	public List<Completion> getCompletions() {
		return completions
			.getProduct()
			.orElse(new ArrayList<>());
	}

	@Override
	public Outline getCurrentAst() {
		// Use this method to add markers to the file
		errors
			.getProduct()
			.ifPresent(errs -> { 
			clearMarkers();
			errs.forEach(error -> {
				switch (error.getLevel()) {
				case "error":
					addErrorMarker(error.getDescription(), error.getStartOffset(), error.getLength());
					break;
				case "warning":
					addWarningMarker(error.getDescription(), error.getStartOffset(), error.getLength());
					break;
				default:
					addInfoMarker(error.getDescription(), error.getStartOffset(), error.getLength());
				}
			});
			flushMarkers();
		});

		return outline
			.getProduct()
		    .orElse(null);
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}

	private void addErrorMarker(String message, int startOffset, int length) {
		addMarker(message, errorSeverity, startOffset, length);
	}

	private void addWarningMarker(String message, int startOffset, int length) {
		addMarker(message, warningSeverity, startOffset, length);
	}

	private void addInfoMarker(String message, int startOffset, int length) {
		addMarker(message, infoSeverity, startOffset, length);
	}

	private void addMarker(String message, Map<String, Object> attributes, int startOffset, int length) {
		getHandler().handleSimpleMessage(message, startOffset, startOffset + length - 1, 0, 0, 0, 0, attributes);
	}

	private void clearMarkers() {
		getHandler().clearMessages();
	}

	private void flushMarkers() {
		getHandler().endMessages();
	}
}
