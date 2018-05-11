package gr.cite.femme.core.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.cite.femme.core.model.Element;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElementList<T extends Element> {
	
	@JsonProperty("elements")
	private List<T> elements;
	
	@JsonProperty("size")
	private int size;
	
	@JsonProperty("total")
	private Integer total;
	
	public ElementList() { }
	
	public ElementList(List<T> elements) {
		this.elements = elements;
		this.size = elements.size();
	}
	
	public ElementList(List<T> elements, Integer total) {
		this.elements = elements;
		this.size = elements.size();
		this.total = total;
	}

	public List<T> getElements() {
		return elements;
	}

	public void setElements(List<T> elements) {
		this.elements = elements;
	}

	public int getSize() {
		return this.elements.size();
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public Integer getTotal() {
		return total;
	}
	
	public void setTotal(Integer total) {
		this.total = total;
	}
}
