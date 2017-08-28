package gr.cite.femme.core.dto;

import java.util.List;

import gr.cite.femme.core.model.Element;

public class ElementList<T extends Element> {
	private List<T> elements;
	private int size;
	
	public ElementList() {
	}
	
	public ElementList(List<T> elements) {
		this.elements = elements;
		this.size = elements.size();
	}

	public List<T> getElements() {
		return elements;
	}

	public void setElements(List<T> elements) {
		this.elements = elements;
	}

	public Integer getSize() {
		return this.elements.size();
	}

	public void setSize(int size) {
		this.size = size;
	}


}
