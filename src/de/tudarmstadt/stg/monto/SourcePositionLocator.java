package de.tudarmstadt.stg.monto;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.ISourcePositionLocator;

import de.tudarmstadt.stg.monto.token.Token;

public class SourcePositionLocator implements ISourcePositionLocator {

	@Override
	public Object findNode(Object astRoot, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object findNode(Object astRoot, int startOffset, int endOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStartOffset(Object obj) {
		return ((Token) obj).getOffset();
	}

	@Override
	public int getEndOffset(Object obj) {
		Token token = ((Token) obj);
		return token.getOffset() + token.getLength() - 1;
	}

	@Override
	public int getLength(Object obj) {
		return ((Token) obj).getOffset();
	}

	@Override
	public IPath getPath(Object node) {
		// TODO Auto-generated method stub
		return null;
	}

}
