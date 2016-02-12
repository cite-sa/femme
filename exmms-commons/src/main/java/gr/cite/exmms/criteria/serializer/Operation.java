package gr.cite.exmms.criteria.serializer;

public enum Operation {
	EXPRESSION("expression"), EXISTS("exists"), IS_PARENT_OF("isParentOf"), IS_CHILD_OF("isChildOf"),
	OR("or"), AND("and");

	private final String value;

	Operation(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
