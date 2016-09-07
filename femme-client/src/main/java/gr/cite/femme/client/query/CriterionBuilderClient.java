package gr.cite.femme.client.query;

public class CriterionBuilderClient {
	
	public static OperatorClient root() {
		CriterionClient criterion = new CriterionClient();
		return criterion.root();
	}
	
}
