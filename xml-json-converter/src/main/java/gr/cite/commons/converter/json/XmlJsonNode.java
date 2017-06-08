package gr.cite.commons.converter.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(Include.NON_NULL)
@JsonSerialize(using=XmlJsonNodeSerializer.class)
public class XmlJsonNode {
	
	private String name;
	
	private LinkedHashMap<String, String> namespaces;
	
	private LinkedHashMap<String, String> attributes;
	
	private List<XmlJsonNode> children;
	
	private String text;
	
	private XmlJsonNode parent;
	
	
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

	public List<XmlJsonNode> getChildren() {
		return children;
	}

	public void setChildren(List<XmlJsonNode> children) {
		this.children = children;
	}
	
	public void addChild(XmlJsonNode child) {
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

	public XmlJsonNode getParent() {
		return parent;
	}

	public void setParent(XmlJsonNode parent) {
		this.parent = parent;
	}
	
	/*@Override
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
				builder.append("\"").append(entry.getKey()).append("\"");
				builder.append(":");
				builder.append("\"").append(entry.getValue()).append("\"");
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
		
		*//*Iterator<Entry<String, XmlJsonNode>> childrenIterator = children.entrySet().iterator();
		while (childrenIterator.hasNext()) {
			Entry<String, XmlJsonNode> entry = childrenIterator.next();
			builder.append("\"" + entry.getKey() + "\"");
			builder.append(":");
			builder.append(entry.getValue().toString());
			if (childrenIterator.hasNext()) {
				builder.append(",");
			}
			
		}*//*
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
	}*/
}

class XmlJsonNodeSerializer extends JsonSerializer<XmlJsonNode> {
	private static final String ATTRIBUTES = "@";
	private static final String NAMESPACES = "ns";
	private static final String TEXT = "#text";

	@Override
	public void serialize(XmlJsonNode xmlJsonNode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		if (xmlJsonNode != null) {
			if (xmlJsonNode.getNamespaces() == null && xmlJsonNode.getAttributes() == null && xmlJsonNode.getChildren() == null) {

				if (xmlJsonNode.getText() != null) {
					//jsonGenerator.writeString(xmlJsonNode.getText());
					jsonGenerator.writeStartObject();
					jsonGenerator.writeObjectField(XmlJsonNodeSerializer.TEXT, xmlJsonNode.getText());
					jsonGenerator.writeEndObject();
				} else {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeEndObject();
					//jsonGenerator.writeNull();
				}
			} else {
				jsonGenerator.writeStartObject();
				if (xmlJsonNode.getNamespaces() != null) {
					jsonGenerator.writeObjectField(XmlJsonNodeSerializer.NAMESPACES, xmlJsonNode.getNamespaces());
				}
				if (xmlJsonNode.getAttributes() != null) {
					jsonGenerator.writeObjectField(XmlJsonNodeSerializer.ATTRIBUTES, xmlJsonNode.getAttributes());
				}
				if (xmlJsonNode.getChildren() != null) {
					List<String> names = xmlJsonNode.getChildren().stream().map(XmlJsonNode::getName).collect(Collectors.toList());

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

					for (String name : duplicates) {
						jsonGenerator.writeArrayFieldStart(name);
						for (XmlJsonNode node : xmlJsonNode.getChildren()) {
							if (node.getName().equals(name)) {
								jsonGenerator.writeObject(node);
							}
						}
						jsonGenerator.writeEndArray();
					}

					for (String name : unique) {
						for (XmlJsonNode node : xmlJsonNode.getChildren()) {
							if (node.getName().equals(name)) {
								jsonGenerator.writeFieldName(node.getName());
								jsonGenerator.writeObject(node);
							}
						}
					}
				}
				if (xmlJsonNode.getText() != null) {
					jsonGenerator.writeStringField(XmlJsonNodeSerializer.TEXT, xmlJsonNode.getText());
				}
				jsonGenerator.writeEndObject();
			}
		}
	}
	
}
