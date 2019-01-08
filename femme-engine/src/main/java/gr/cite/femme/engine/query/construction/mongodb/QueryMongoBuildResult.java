package gr.cite.femme.engine.query.construction.mongodb;

import gr.cite.femme.core.query.construction.QueryBuildResult;
import org.bson.Document;

public class QueryMongoBuildResult implements QueryBuildResult<Document> {
	private Document buildResult;
	
	public QueryMongoBuildResult(Document buildResult) {
		this.buildResult = buildResult;
	}
	
	public void setBuildResult(Document buildResult) {
		this.buildResult = buildResult;
	}
	
	@Override
	public Document get() {
		return this.buildResult;
	}
}
