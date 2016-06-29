package monto.eclipse;

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
import monto.service.error.Error;
import monto.service.highlighting.Token;
import monto.service.outline.Outline;
import monto.service.region.Region;
import monto.service.source.SourceMessage;
import monto.service.types.Language;
import monto.service.types.LongKey;
import monto.service.types.Source;

public class MontoParseController extends ParseControllerBase {

  private String contents = null;
  private Source source = null;
  private Language language = null;
  private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
  private UniversalEditor editor;

  private LongKey versionID = new LongKey(0);

  private VersionIdBasedProductCache<Outline> outlineCache;
  private VersionIdBasedProductCache<List<Token>> tokensCache;
  private VersionIdBasedProductCache<List<Completion>> completionsCache;
  private VersionIdBasedProductCache<List<Error>> errorsCache;


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
    source = new Source(filePath.lastSegment());
    language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());

    outlineCache = new VersionIdBasedProductCache<>();
    tokensCache = new VersionIdBasedProductCache<>();
    completionsCache = new VersionIdBasedProductCache<>();
    errorsCache = new VersionIdBasedProductCache<>();

    outlineCache.setTimeout(500);
    tokensCache.setTimeout(500);
    completionsCache.setTimeout(500);
    errorsCache.setTimeout(500);

    Activator.getDefault().getDemultiplexer().setTarget(source, language)
        .setProductCaches(outlineCache, tokensCache, errorsCache, completionsCache);
  }

  @Override
  public Object parse(String documentText, IProgressMonitor monitor) {
    try {
      contents = documentText;
      if (editor != null) {
        Display.getDefault().syncExec(() -> {
          IRegion region = editor.getSelectedRegion();
          // TODO Do something with this selection
        });
      }

      versionID.increment();
      Activator.getDefault().getDemultiplexer().invalidateProducts(versionID);
      SourceMessage version = new SourceMessage(versionID, source, language, contents);
      Activator.sendSourceMessage(version);
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
    return tokensCache.getProduct().orElse(new ArrayList<>()).stream()
        .filter((token) -> token.inRange(new Region(region.getOffset(), region.getLength())))
        .iterator();
  }

  public List<Completion> getCompletions() {
    return completionsCache.getProduct().orElse(new ArrayList<>());
  }

  @Override
  public Outline getCurrentAst() {
    // Use this method to add markers to the file
    errorsCache.getProduct().ifPresent(errs -> {
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

    return outlineCache.getProduct().orElse(null);
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

  private void addMarker(String message, Map<String, Object> attributes, int startOffset,
      int length) {
    getHandler().handleSimpleMessage(message, startOffset, startOffset + length - 1, 0, 0, 0, 0,
        attributes);
  }

  private void clearMarkers() {
    getHandler().clearMessages();
  }

  private void flushMarkers() {
    getHandler().endMessages();
  }
}
