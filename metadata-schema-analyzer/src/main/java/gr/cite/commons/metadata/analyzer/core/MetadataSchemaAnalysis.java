package gr.cite.commons.metadata.analyzer.core;

import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.commons.utils.hash.HashGeneratorUtils;

import java.util.Set;

public class MetadataSchemaAnalysis {

    private Set<JSONPath> schema;

    private String checksum = null;

    public MetadataSchemaAnalysis(Set<JSONPath> schema) {
        this.schema = schema;
    }

    public Set<JSONPath> getSchema() {
        return schema;
    }

    public void setSchema(Set<JSONPath> schema) {
        this.schema = schema;
        checksum = null;
    }

    public String getChecksum() throws HashGenerationException {
        if (checksum == null) {
            /*MurmurHash3.LongPair hashResult = new MurmurHash3.LongPair();
            String schemaString = schema.toString();

            MurmurHash3.murmurhash3_x64_128(schemaString.getBytes(StandardCharsets.UTF_8), 0, schemaString.length(), 1234567890, hashResult);
            getChecksum = new String(Long.toString(hashResult.val1) + Long.toString(hashResult.val2));*/

            return HashGeneratorUtils.generateMD5(schema.toString());
        }
        return checksum;
    }
}
