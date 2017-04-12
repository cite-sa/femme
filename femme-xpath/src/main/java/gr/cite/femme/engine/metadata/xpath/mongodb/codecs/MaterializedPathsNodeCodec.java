package gr.cite.femme.engine.metadata.xpath.mongodb.codecs;

import gr.cite.femme.engine.metadata.xpath.core.MaterializedPathsNode;
import org.bson.*;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterializedPathsNodeCodec implements CollectibleCodec<MaterializedPathsNode> {

    private static final String ID_KEY = "_id";
    private static final String METADATUM_ID_KEY = "mId";
    private static final String PATH_KEY = "path";
    private static final String PARENT_KEY = "parent";
    private static final String CHILDREN_KEY = "children";
    private static final String NAMESPACES_KEY = "ns";
    private static final String ATTRIBUTES_KEY = "@";
    private static final String NAME_KEY = "name";
    private static final String VALUE_KEY = "value";

    @Override
    public void encode(BsonWriter writer, MaterializedPathsNode value, EncoderContext encoderContext) {
        writer.writeStartDocument();

        if (!documentHasId(value)) {
            generateIdIfAbsentFromDocument(value);
        }

        if (value.getId() != null) {
            writer.writeObjectId(MaterializedPathsNodeCodec.ID_KEY, new ObjectId(value.getId()));
        }
        if (value.getMetadatumId() != null) {
            writer.writeObjectId(MaterializedPathsNodeCodec.METADATUM_ID_KEY, new ObjectId(value.getMetadatumId()));
        }
        if (value.getPath() != null) {
            writer.writeString(MaterializedPathsNodeCodec.PATH_KEY, value.getPath());
        }
        if (value.getParent() != null) {
            writer.writeObjectId(MaterializedPathsNodeCodec.PARENT_KEY, new ObjectId(value.getParent()));
        }
        if (value.getChildren() != null && value.getChildren().size() > 0) {
            writer.writeStartArray(MaterializedPathsNodeCodec.CHILDREN_KEY);
            for (String childId: value.getChildren()) {
                writer.writeObjectId(new ObjectId(childId));
            }
            writer.writeEndArray();
        }
        if (value.getNamespaces() != null && value.getNamespaces().size() > 0) {
            writer.writeStartDocument(MaterializedPathsNodeCodec.NAMESPACES_KEY);
            for (Map.Entry<String, String> namespace: value.getNamespaces().entrySet()) {
                writer.writeString(namespace.getKey(), namespace.getValue());
            }
            writer.writeEndDocument();
        }
        if (value.getAttributes() != null && value.getAttributes().size() > 0) {
            writer.writeStartDocument(MaterializedPathsNodeCodec.ATTRIBUTES_KEY);
            for (Map.Entry<String, String> attribute: value.getAttributes().entrySet()) {
                writer.writeString(attribute.getKey(), attribute.getValue());
            }
            writer.writeEndDocument();
        }
        if (value.getValue() != null) {
            writer.writeString(MaterializedPathsNodeCodec.VALUE_KEY, value.getValue());
        }
        if (value.getName() != null) {
            writer.writeString(MaterializedPathsNodeCodec.NAME_KEY, value.getName());
        }
        if (value.getValue() != null) {
            writer.writeString(MaterializedPathsNodeCodec.VALUE_KEY, value.getValue());
        }
        writer.writeEndDocument();
    }

    @Override
    public MaterializedPathsNode decode(BsonReader reader, DecoderContext decoderContext) {
        String id = null, metadatumId = null, path = null, parent = null, name = null, value = null;
        Map<String, String> namespaces = null, attributes = null;
        List<String> children;

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals(MaterializedPathsNodeCodec.ID_KEY)) {
                id = reader.readObjectId().toString();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.METADATUM_ID_KEY)) {
                metadatumId = reader.readObjectId().toString();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.PATH_KEY)) {
                path = reader.readString();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.PARENT_KEY)) {
                parent = reader.readObjectId().toString();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.CHILDREN_KEY)) {
                children = new ArrayList<>();
                reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    children.add(reader.readObjectId().toString());
                }
                reader.readEndArray();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.NAMESPACES_KEY)) {
                namespaces = new HashMap<>();
                reader.readStartDocument();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    /*String namespacePrefix = reader.readName();
                    String namespaceValue = reader.readString();*/
                    namespaces.put(reader.readName(), reader.readString());
                }
                reader.readEndDocument();
            }  else if (fieldName.equals(MaterializedPathsNodeCodec.ATTRIBUTES_KEY)) {
                attributes = new HashMap<>();
                reader.readStartDocument();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    attributes.put(reader.readName(), reader.readString());
                }
                reader.readEndDocument();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.NAME_KEY)) {
                name = reader.readString();
            } else if (fieldName.equals(MaterializedPathsNodeCodec.VALUE_KEY)) {
                value = reader.readString();
            }
        }
        reader.readEndDocument();

        MaterializedPathsNode node = new MaterializedPathsNode();
        node.setId(id);
        node.setMetadatumId(metadatumId);
        node.setPath(path);
        node.setNamespaces(namespaces);
        node.setAttributes(attributes);
        node.setName(name);
        node.setValue(value);

        return node;
    }

    @Override
    public Class<MaterializedPathsNode> getEncoderClass() {
        return MaterializedPathsNode.class;
    }

    @Override
    public MaterializedPathsNode generateIdIfAbsentFromDocument(MaterializedPathsNode materializedPathsNode) {
        if (!documentHasId(materializedPathsNode)) {
            materializedPathsNode.setId(new ObjectId().toString());
        }
        return materializedPathsNode;
    }

    @Override
    public boolean documentHasId(MaterializedPathsNode materializedPathsNode) {
        return materializedPathsNode.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(MaterializedPathsNode materializedPathsNode) {
        if (!documentHasId(materializedPathsNode)) {
            throw new IllegalStateException("The materializedPathsNode does not contain an _id");
        }
        return new BsonString(materializedPathsNode.getId());
    }

}
