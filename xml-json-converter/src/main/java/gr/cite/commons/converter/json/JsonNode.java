package gr.cite.commons.converter.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(Include.NON_NULL)
@JsonSerialize(using=JsonNodeSerializer.class)
public class JsonNode {
	
	private String name;
	
	private LinkedHashMap<String, String> namespaces;
	
	private LinkedHashMap<String, String> attributes;
	
	private List<JsonNode> children;
	
	private String text;
	
	private JsonNode parent;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedHashMap<String, String> getAttributes() {
		return attributes;
	}

	public LinkedHashMap<String, String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(LinkedHashMap<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	public void setAttributes(LinkedHashMap<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<JsonNode> getChildren() {
		return children;
	}

	public void setChildren(List<JsonNode> children) {
		this.children = children;
	}
	
	public void addChild(JsonNode child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public JsonNode getParent() {
		return parent;
	}

	public void setParent(JsonNode parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		
		if (attributes != null && attributes.size() > 0) {
			builder.append("\"attributes\": {");
			Iterator<Entry<String, String>> attributesIterator = attributes.entrySet().iterator();
			while (attributesIterator.hasNext()) {
				Entry<String, String> entry = attributesIterator.next();
				builder.append("\"" + entry.getKey() + "\"");
				builder.append(":");
				builder.append("\"" + entry.getValue() + "\"");
				if (attributesIterator.hasNext()) {
					builder.append(",");
				}
				
			}
			
			builder.append("}");
		}
		
		if (namespaces != null && namespaces.size() > 0) {
			if (attributes != null && attributes.size() > 0) {
				builder.append(",");
			}
			builder.append("\"namespaces\": {");
			Iterator<Entry<String, String>> namespacesIterator = namespaces.entrySet().iterator();
			while (namespacesIterator.hasNext()) {
				Entry<String, String> entry = namespacesIterator.next();
				builder.append("\"" + entry.getKey() + "\"");
				builder.append(":");
				builder.append("\"" + entry.getValue() + "\"");
				if (namespacesIterator.hasNext()) {
					builder.append(",");
				}
				
			}
			builder.append("}");
		}
		
		if (namespaces != null && namespaces.size() > 0 && children.size() > 0) {
			builder.append(",");
		} else if (attributes != null && attributes.size() > 0 && children.size() > 0) {
				builder.append(",");
		}
		
		/*Iterator<Entry<String, JsonNode>> childrenIterator = children.entrySet().iterator();
		while (childrenIterator.hasNext()) {
			Entry<String, JsonNode> entry = childrenIterator.next();
			builder.append("\"" + entry.getKey() + "\"");
			builder.append(":");
			builder.append(entry.getValue().toString());
			if (childrenIterator.hasNext()) {
				builder.append(",");
			}
			
		}*/
		for (int i = 0; i < children.size(); i++) {
			builder.append("\"" + children.get(i).getName() + "\"");
			builder.append(":");
			builder.append(children.get(i).toString());
			if (i < children.size() - 1) {
				builder.append(",");
			}
			
		}
		
		builder.append("}");
		return builder.toString();
	}
}

class JsonNodeSerializer extends JsonSerializer<JsonNode> {
	
	private static final String ATTRIBUTES = "@";
	
	private static final String NAMESPACES = "ns";

	@Override
	public void serialize(JsonNode jsonNode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException, JsonProcessingException {
		
		if (jsonNode.getNamespaces() == null && jsonNode.getAttributes() == null && jsonNode.getChildren() == null) {
			if (jsonNode.getText() != null) {
				jsonGenerator.writeString(jsonNode.getText());
			} else {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeEndObject();
			}
		} else {
			jsonGenerator.writeStartObject();
			if (jsonNode.getNamespaces() != null) {
				jsonGenerator.writeObjectField(JsonNodeSerializer.NAMESPACES, jsonNode.getNamespaces());
			}
			if (jsonNode.getAttributes() != null) {
				jsonGenerator.writeObjectField(JsonNodeSerializer.ATTRIBUTES, jsonNode.getAttributes());
			}
			if (jsonNode.getChildren() != null) {
				List<String> names = jsonNode.getChildren()
						.stream()
						.map(child -> {return child.getName();})
						.collect(Collectors.toList());
				
				Set<String> duplicates = names.stream()
						.filter(childName -> Collections.frequency(names, childName) > 1)
						.collect(Collectors.toSet());
				
				List<String> unique = names.stream()
						.filter(childName -> Collections.frequency(names, childName) <= 1)
						.collect(Collectors.toList());
				
				/*if (unique.size() > 0) {
					System.out.println(unique);
				}
				if (duplicates.size() > 0) {
					System.out.println(duplicates);
				}*/
				
				for (String name: duplicates) {
					jsonGenerator.writeArrayFieldStart(name);
					for (JsonNode node: jsonNode.getChildren()) {
						if (node.getName().equals(name)) {
							jsonGenerator.writeObject(node);							
						}
					}
					jsonGenerator.writeEndArray();
				}
				
				for (String name: unique) {
					for (JsonNode node: jsonNode.getChildren()) {
						if (node.getName().equals(name)) {
							jsonGenerator.writeFieldName(node.getName());
							jsonGenerator.writeObject(node);							
						}
					}
				}
			}
			if (jsonNode.getText() != null) {
				jsonGenerator.writeStringField("text", jsonNode.getText());
			}
			jsonGenerator.writeEndObject();
		}
	}
	
}
