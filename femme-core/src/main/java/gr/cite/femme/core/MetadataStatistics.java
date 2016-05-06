package gr.cite.femme.core;

import java.time.Duration;
import java.time.Instant;

public class MetadataStatistics {
	private Instant firstQueried;
	
	private Instant lastQueried;
	
	private int numOfQueries;
	
	private double frequency;
	
	public MetadataStatistics() {
		firstQueried = Instant.now();
		updateStatictics();
	}
	
	public void updateStatictics() {
		numOfQueries++;
		lastQueried = Instant.now();
		frequency = Duration.between(firstQueried, lastQueried).toMillis() / numOfQueries;
	}
	
	public Instant getFirstQueried() {
		return firstQueried;
	}

	public void setFirstQueried(Instant firstQueried) {
		this.firstQueried = firstQueried;
	}

	public Instant getLastQueried() {
		return lastQueried;
	}

	public void setLastQueried(Instant lastQueried) {
		this.lastQueried = lastQueried;
	}

	public int getNumOfQueries() {
		return numOfQueries;
	}

	public void setNumOfQueries(int numOfQueries) {
		this.numOfQueries = numOfQueries;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	
}
