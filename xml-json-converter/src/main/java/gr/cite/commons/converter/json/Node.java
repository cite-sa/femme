package gr.cite.commons.converter.json;

public class Node {
	private String fieldName;
	private boolean isArray;

	public Node(String fieldName, boolean isArray) {
		this.fieldName = fieldName;
		this.isArray = isArray;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean array) {
		isArray = array;
	}
}
