package de.tudarmstadt.stg.monto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.ParseControllerBase;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;

import de.tudarmstadt.stg.monto.client.MontoClient;
import de.tudarmstadt.stg.monto.message.Contents;
import de.tudarmstadt.stg.monto.message.Language;
import de.tudarmstadt.stg.monto.message.Selection;
import de.tudarmstadt.stg.monto.message.Source;
import de.tudarmstadt.stg.monto.message.StringContent;

public class MontoParseController extends ParseControllerBase {
	
	private final MontoClient client;
	
	public MontoParseController() {
		this(Activator.getDefault().getMontoClient());
	}
	
	public MontoParseController(MontoClient client) {
		this.client = client;
	}

	@Override
	public Object parse(String documentText, IProgressMonitor monitor) {
		
		final Source source = new Source(this.getPath().toString());;
		final org.eclipse.imp.language.Language impLanguage = org.eclipse.imp.language.LanguageRegistry.findLanguage(getPath(), getDocument());
		final Language language = new Language(impLanguage.toString());
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
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISourcePositionLocator getSourcePositionLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILanguageSyntaxProperties getSyntaxProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getTokenIterator(IRegion region) {
		// TODO Auto-generated method stub
		return null;
	}
}
