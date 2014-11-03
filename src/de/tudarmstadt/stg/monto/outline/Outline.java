package de.tudarmstadt.stg.monto.outline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.tudarmstadt.stg.monto.region.IRegion;
import de.tudarmstadt.stg.monto.region.Region;

public class Outline extends Region {
	
	private String description;
	private Optional<String> icon;
	private List<Outline> childs;
	
	public Outline(String description, IRegion region, String icon, List<Outline> childs) {
		super(region.getStartOffset(),region.getLength());
		this.description = description;
		this.icon = Optional.ofNullable(icon);
		this.childs = childs;
	}
	
	public Outline(String description, IRegion region, String icon) {
		this(description, region, icon, new ArrayList<>());
	}
	
	public void addChild(Outline outline) {
		childs.add(outline);
	}
	
	public List<Outline> getChilds() {
		return childs;
	}

	public Optional<String> getIcon() {
		return icon;
	}
	
	public boolean isLeaf() {
		return getChilds().size() == 0;
	}

	public String getDescription() {
		return description;
	}

	public IRegion getIdentifier() {
		return this;
	}
	
	@Override
	public String toString() {
		return description;
	}
}
