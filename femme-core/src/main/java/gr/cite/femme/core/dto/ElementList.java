package gr.cite.femme.core.dto;

import java.util.List;

import gr.cite.femme.core.model.Element;

public class ElementList<T extends Element> {
	private List<T> elements;
	
	public ElementList() {
	}
	
	public ElementList(List<T> elements) {
		this.elements = elements;
	}

	public List<T> getElements() {
		return elements;
	}

	public void setElements(List<T> elements) {
		this.elements = elements;
	}
}
