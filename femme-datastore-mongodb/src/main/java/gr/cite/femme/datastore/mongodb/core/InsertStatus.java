package gr.cite.femme.datastore.mongodb.core;

public enum InsertStatus {
	INACTIVE(0),
	ACTIVE(1),
	PENDING(2);
	
	private int status;
	
	private InsertStatus(int status) {
		
	}
	
	public int getStatus() {
		return status;
	}
}
