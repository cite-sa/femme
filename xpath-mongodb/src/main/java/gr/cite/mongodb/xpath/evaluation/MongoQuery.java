package gr.cite.mongodb.xpath.evaluation;

public class MongoQuery {
	
	private StringBuilder queryBuilder;

	public StringBuilder getQueryBuilder() {
		return this.queryBuilder;
	}

	public void setQueryBuilder(StringBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}
	
	public String build() {
		return queryBuilder.toString();
	}

}
