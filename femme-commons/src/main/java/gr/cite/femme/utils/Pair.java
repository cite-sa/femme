package gr.cite.femme.utils;

public class Pair<T, S> {
	private T first;
	private S second;
	
	public Pair() {
		
	}
	public Pair(T first, S second) {
			this.first = first;
			this.second = second;
	}
	public T getFirst() {
		return this.first;
	}
	public void setFirst(T first) {
		this.first = first;
	}
	public S getSecond() {
		return this.second;
	}
	public void setSecond(S second) {
		this.second = second;
	}
}
