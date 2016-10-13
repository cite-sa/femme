package gr.cite.femme.datastore.mongodb.core;

public enum Status {
	INACTIVE(0),
	ACTIVE(1),
	PENDING(2);
	
	private int status;
	
	private Status(int status) {
		
	}
	
	public int getStatus() {
		return status;
	}
}
