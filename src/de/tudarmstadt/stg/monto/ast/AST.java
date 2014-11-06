package de.tudarmstadt.stg.monto.ast;

import de.tudarmstadt.stg.monto.region.IRegion;

public interface AST extends IRegion {
	public void accept(ASTVisitor visitor);
}
