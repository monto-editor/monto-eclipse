package monto.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;

import monto.service.region.IRegion;

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
    if (obj instanceof IRegion)
      return ((IRegion) obj).getStartOffset();
    else if (obj instanceof ModelTreeNode)
      return getStartOffset(((ModelTreeNode) obj).getASTNode());
    else
      throw new IllegalArgumentException(
          "The argument for SourcePositionLocator.getStartOffset is neither a token nor a node: "
              + obj.getClass().getSimpleName());
  }

  @Override
  public int getEndOffset(Object obj) {
    return getStartOffset(obj) + getLength(obj) - 1;
  }

  @Override
  public int getLength(Object obj) {
    if (obj instanceof IRegion)
      return ((IRegion) obj).getLength();
    else if (obj instanceof ModelTreeNode)
      return getLength(((ModelTreeNode) obj).getASTNode());
    else
      throw new IllegalArgumentException(
          "The argument for SourcePositionLocator.getLength is neither a token nor a node: "
              + obj.getClass().getSimpleName());
  }

  @Override
  public IPath getPath(Object node) {
    return null;
  }
}
