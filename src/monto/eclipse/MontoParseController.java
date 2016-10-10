package monto.eclipse;

import java.io.File;
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

import monto.eclipse.demultiplex.SinkDemultiplexer;
import monto.eclipse.demultiplex.VersionIdBasedProductCache;
import monto.service.completion.Completion;
import monto.service.completion.CompletionRequest;
import monto.service.error.Error;
import monto.service.gson.GsonMonto;
import monto.service.highlighting.Token;
import monto.service.outline.Outline;
import monto.service.product.Products;
import monto.service.region.Region;
import monto.service.source.SourceMessage;
import monto.service.types.Language;
import monto.service.types.LongKey;
import monto.service.types.ServiceId;
import monto.service.types.Source;

public class MontoParseController extends ParseControllerBase {

  private String contents = null;
  private Source source = null;
  private Language language = null;
  private final ISourcePositionLocator sourcePositionLocator = new SourcePositionLocator();
  private UniversalEditor editor;

  private LongKey versionId = new LongKey(0);
  private int codeCompletionCommandMessageId = 0;

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
    source = new Source(project.getName() + File.separator + filePath.toString());
    // filePath.toString() includes to src/ directory,
    // which is correct for the physical name of a Source
    language = new Language(LanguageRegistry.findLanguage(getPath(), getDocument()).getName());

    outlineCache = new VersionIdBasedProductCache<>("outline",
        productMessage -> GsonMonto.fromJson(productMessage, Outline.class));
    tokensCache = new VersionIdBasedProductCache<>("tokens",
        productMessage -> GsonMonto.fromJsonArray(productMessage, Token[].class));
    completionsCache = new VersionIdBasedProductCache<>("completions",
        productMessage -> GsonMonto.fromJsonArray(productMessage, Completion[].class));
    errorsCache = new VersionIdBasedProductCache<>("errors",
        productMessage -> GsonMonto.fromJsonArray(productMessage, Error[].class));

    outlineCache.setTimeout(500);
    tokensCache.setTimeout(500);
    completionsCache.setTimeout(500);
    errorsCache.setTimeout(500);

    SinkDemultiplexer demultiplexer = Activator.getDefault().getDemultiplexer();
    demultiplexer.addProductListener(Products.OUTLINE, outlineCache::onProductMessage);
    demultiplexer.addProductListener(Products.TOKENS, tokensCache::onProductMessage);
    demultiplexer.addProductListener(Products.COMPLETIONS, completionsCache::onProductMessage);
    demultiplexer.addProductListener(Products.ERRORS, errorsCache::onProductMessage);
    demultiplexer.addProductListener(Products.LOGICAL_SOURCE_NAME, productMessage -> {
      if (!source.getLogicalName().isPresent() && productMessage.getSource().equals(source)) {
        // resend SourceMessage with logical name
        Source sourceWithLogicalName =
            GsonMonto.fromJson(productMessage.getContents(), Source.class);
        source = sourceWithLogicalName;
        parse(contents, null);
      }
    });
  }

  @Override
  public Object parse(String documentText, IProgressMonitor monitor) {
    try {
      contents = documentText;
      versionId.increment();
      invalidateAllProducts(versionId);
      SourceMessage sourceMessage = new SourceMessage(versionId, source, language, contents);
      Activator.sendSourceMessage(sourceMessage);
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
    completionsCache.invalidateProduct(versionId);
    IRegion region = editor.getSelectedRegion();
    // TODO: don't hardcode serviceId here, but rather filter received services at discovery by
    // product and language
    Activator.sendCommandMessage(CompletionRequest.createCommandMessage(
        codeCompletionCommandMessageId++, 0, new ServiceId("javaCodeCompletion"), source,
        new Region(region.getOffset(), region.getLength())));
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

  private void invalidateAllProducts(LongKey newVersionId) {
    outlineCache.invalidateProduct(newVersionId);
    tokensCache.invalidateProduct(newVersionId);
    errorsCache.invalidateProduct(newVersionId);
    completionsCache.invalidateProduct(newVersionId);
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
