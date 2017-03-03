package gr.cite.femme.metadata.xpath.core;

import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.commons.metadata.analyzer.core.MetadataSchemaAnalysis;
import gr.cite.commons.utils.hash.HashGenerationException;

import java.util.Set;

public class MetadataSchema {

    public MetadataSchema() {

    }

    public MetadataSchema(MetadataSchemaAnalysis metadataSchemaAnalysis) throws HashGenerationException {
        this.schema = metadataSchemaAnalysis.getSchema();
        this.hash = metadataSchemaAnalysis.hash();
    }

    private String id;

    private Set<JSONPath> schema;

    private String hash;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<JSONPath> getSchema() {
        return schema;
    }

    public void setSchema(Set<JSONPath> schema) {
        this.schema = schema;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
