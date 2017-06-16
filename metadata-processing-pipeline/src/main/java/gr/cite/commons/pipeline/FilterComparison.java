package gr.cite.commons.pipeline;

@FunctionalInterface
public interface FilterComparison<T> {
	public boolean compare(T result, T value);
}
