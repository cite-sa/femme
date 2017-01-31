package gr.cite.femme.metadata.xpath.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MaterializedPathsNode {

    @JsonProperty("id")
    private String id;

    @JsonProperty("metadatumId")
    private String metadatumId;

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("children")
    private List<String> children;

    @JsonProperty("path")
    private String path;

    @JsonProperty("ns")
    private Map<String, String> namespaces;

    @JsonProperty("@")
    private Map<String, String> attributes;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMetadatumId() {
        return metadatumId;
    }

    public void setMetadatumId(String metadatumId) {
        this.metadatumId = metadatumId;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
