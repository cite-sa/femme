package gr.cite.femme.model;

public enum Status {
	INACTIVE(0),
	ACTIVE(1),
	PENDING(2);
	
	private int statusCode;
	
	private Status(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}

	public static Status getEnum(int code) {
		switch (code) {
		case 0:
			return Status.INACTIVE;
		case 1:
			return Status.ACTIVE;
		case 22:
			return Status.PENDING;
		
		default:
			return null;
		}
	}
}
