package monto.eclipse;

import static monto.eclipse.OptionalUtils.withException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import monto.service.completion.Completion;
import monto.service.completion.Completions;
import monto.service.message.Language;
import monto.service.message.LongKey;
import monto.service.message.Selection;
import monto.service.message.Source;
import monto.service.message.VersionMessage;
import monto.service.outline.Outline;
import monto.service.outline.Outlines;
import monto.service.region.Region;
import monto.service.token.Token;
import monto.service.token.Tokens;

public class MontoParseController extends ParseControllerBase {

	private String contents = null;
	private Source source = null;
	private Language language = null;
	private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
	private UniversalEditor editor;

	private LongKey versionID = new LongKey(0);
	Service<Outline> outline;
	Service<List<Token>> tokens;
	Service<List<Completion>> completions;
	List<Service<?>> services = new ArrayList<>();
	
	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		super.initialize(filePath, project, handler);
		source = new Source(String.format("/%s/%s",project.getName(), filePath.toPortableString()));
		language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());
		outline = new Service<Outline>(source, language, "outliner", withException(Outlines::decode));
		tokens = new Service<List<Token>>(source, language, "tokenizer", withException(Tokens::decode));
		completions = new Service<List<Completion>>(source, language, "completioner", withException(Completions::decode));
		services.add(outline);
		services.add(completions);
		services.add(tokens);
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
			if(editor != null) {
				Display.getDefault().syncExec(() -> {
					IRegion region = editor.getSelectedRegion();
					selections.add(new Selection(region.getOffset(), region.getLength()));
				});
			}
			
			versionID.increment();
			services.forEach(service -> service.invalidateProduct(versionID));
			VersionMessage version = new VersionMessage(versionID,source,language,contents,selections);
			Activator.sendMessage(version);			
		} catch (Exception e) {
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
	public ParseResult getCurrentAst() {
		return outline
			.getProduct()
		    .map(o -> new ParseResult(o, contents))
		    .orElse(null);
	}

	public void setEditor(UniversalEditor editor) {
		this.editor = editor;
	}
}
