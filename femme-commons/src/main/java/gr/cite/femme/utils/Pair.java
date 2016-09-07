package gr.cite.femme.utils;

public class Pair<T, S> {
	
	private T left;
	
	private S right;
	
	public Pair() {
		
	}
	
	public Pair(T left, S right) {
			this.left = left;
			this.right = right;
	}
	
	public T getLeft() {
		return this.left;
	}
	
	public void setLeft(T left) {
		this.left = left;
	}
	
	public S getRight() {
		return this.right;
	}
	
	public void setRight(S right) {
		this.right = right;
	}
}
