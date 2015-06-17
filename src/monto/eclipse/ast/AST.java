package monto.eclipse.ast;

import monto.eclipse.region.IRegion;

public interface AST extends IRegion {
	public void accept(ASTVisitor visitor);
}
