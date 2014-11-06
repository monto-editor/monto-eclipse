package de.tudarmstadt.stg.monto.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NonTerminal implements AST {

	private String name;
	private List<AST> childs;
	
	public NonTerminal(String name, List<AST> childs) {
		this.name = name;
		this.childs = childs;
	}
	
	public NonTerminal(String name, AST ... childs) {
		this(name, new ArrayList<AST>(Arrays.asList(childs)));
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public AST getChild(int i) {
		return childs.get(i);
	}
	
	public List<AST> getChilds() {
		return childs;
	}

	public void addChild(AST a) {
		childs.add(a);
	}

	@Override
	public int getStartOffset() {
		return childs.get(0).getStartOffset();
	}
	
	@Override
	public int getEndOffset() {
		return childs.get(childs.size()-1).getEndOffset();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
