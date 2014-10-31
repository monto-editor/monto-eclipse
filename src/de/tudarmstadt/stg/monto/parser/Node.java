package de.tudarmstadt.stg.monto.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node implements AST {

	private String name;
	private List<AST> childs;
	
	public Node(String name, List<AST> childs) {
		this.name = name;
		this.childs = childs;
	}
	
	public Node(String name, AST ... childs) {
		this(name, new ArrayList<AST>(Arrays.asList(childs)));
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public List<AST> getChilds() {
		return childs;
	}

	public void addChild(AST a) {
		childs.add(a);
	}
	
}
