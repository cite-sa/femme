package gr.cite.commons.metadata.analyzer.core;

public class JSONPath {

	private String path;

	private boolean array;

	public JSONPath() {
	}

	public JSONPath(String path, boolean array) {
		this.path = path;
		this.array = array;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	@Override
	public String toString() {
		return path + ";" + array;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JSONPath jsonPath = (JSONPath) o;

		if (isArray() != jsonPath.isArray()) return false;
		return getPath() != null ? getPath().equals(jsonPath.getPath()) : jsonPath.getPath() == null;
	}

	@Override
	public int hashCode() {
		int result = getPath() != null ? getPath().hashCode() : 0;
		result = 31 * result + (isArray() ? 1 : 0);
		return result;
	}
}
