package gr.cite.femme.metadata.xpath.mongodb.evaluation;

import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;

import java.util.List;

public class MongoXPathResult {

    private List<MaterializedPathsNode> result;

    public List<MaterializedPathsNode> getResult() {
        return result;
    }

    public void setResult(List<MaterializedPathsNode> result) {
        this.result = result;
    }
}
