package gr.cite.femme.semantic.search.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gr.cite.femme.semantic.search.config.AppConfig;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ElasticQueryBuilder {

    private static final ObjectMapper mapper = new ObjectMapper();
    /* Fields*/
    @JsonProperty("fields")
    private List<String> fields;
    @JsonProperty("terms")
    private Map<String, List<String>> terms;
    @JsonProperty("includeFields")
    private List<String> includeFields;
    @JsonProperty("excludeFields")
    private List<String> excludeFields;
    @JsonProperty("operand")
    private String operand;
    @JsonProperty("rangeField")
    private String rangeField;
    @JsonProperty("from")
    private int from;
    @JsonProperty("size")
    private int size = 10;

    /* Conf */
    @JsonProperty("range")
    private Map<String, String> range;
    @JsonProperty("sort")
    private Map<String, String> sort;
    @JsonProperty("filterTerms")
    private Map<String, String> filterTerms;
    @JsonProperty("filterRange")
    private Map<String, Object> filterRange;
    @JsonProperty("exactMatch")
    private boolean exactMatch;
    @JsonProperty("termsWithFields")
    private List<Map<String,Object>> boolQuery;
    @JsonProperty("function_score")
    private Map<String, Object> functionScore;
    @JsonProperty("function_score")
    private Map<String, Integer> termsWithWeight;

    @JsonProperty("boost")
    private int boost = 5;
    @JsonProperty("min_score")
    private double minScore = 0.0;

    @JsonProperty("weight")
    private int weight = 1;

    @JsonProperty("query")
    private ElasticQueryBuilder query = new ElasticQueryBuilder();

    public List<String> getFields() {
        return fields;
    }

    public ElasticQueryBuilder setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public Map<String, List<String>> getTerms() {
        return terms;
    }

    public ElasticQueryBuilder setTerms(Map<String, List<String>> terms) {
        this.terms = terms;
        return this;
    }

    public List<String> getIncludeFields() {
        return includeFields;
    }

    public ElasticQueryBuilder setIncludeFields(List<String> includeFields) {
        this.includeFields = includeFields;
        return this;
    }

    public List<String> getExcludeFields() {
        return excludeFields;
    }

    public ElasticQueryBuilder setExcludeFields(List<String> excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    public String getOperand() {
        return operand;
    }

    public ElasticQueryBuilder setOperand(String operand) {
        this.operand = operand;
        return this;
    }

    public String getRangeField() {
        return rangeField;
    }

    public ElasticQueryBuilder setRangeField(String rangeField) {
        this.rangeField = rangeField;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public ElasticQueryBuilder setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getSize() {
        return size;
    }

    public ElasticQueryBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public Map<String, String> getRange() {
        return range;
    }

    public ElasticQueryBuilder setRange(Map<String, String> range) {
        this.range = range;
        return this;
    }

    public Map<String, String> getSort() {
        return sort;
    }

    public ElasticQueryBuilder setSort(Map<String, String> sort) {
        this.sort = sort;
        return this;
    }

    public Map<String, String> getFilterTerms() {
        return filterTerms;
    }

    public ElasticQueryBuilder setFilterTerms(Map<String, String> filterTerms) {
        this.filterTerms = filterTerms;
        return this;
    }

    public Map<String, Object> getFilterRange() {
        return filterRange;
    }

    public ElasticQueryBuilder setFilterRange(Map<String, Object> filterRange) {
        this.filterRange = filterRange;
        return this;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public ElasticQueryBuilder setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
        return this;
    }

    public List<Map<String, Object>> getBoolQuery() {
        return boolQuery;
    }

    public ElasticQueryBuilder setBoolQuery(List<Map<String, Object>> boolQuery) {
        this.boolQuery = boolQuery;
        return this;
    }

    public Map<String, Object> getFunctionScore() {
        return functionScore;
    }

    public ElasticQueryBuilder setFunctionScore(Map<String, Object> functionScore) {
        this.functionScore = functionScore;
        return this;
    }

    public Map<String, Integer> getTermsWithWeight() {
        return termsWithWeight;
    }

    public ElasticQueryBuilder setTermsWithWeight(Map<String, Integer> termsWithWeight) {
        this.termsWithWeight = termsWithWeight;
        return this;
    }

    public int getBoost() {
        return boost;
    }

    public ElasticQueryBuilder setBoost(int boost) {
        this.boost = boost;
        return this;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public int getWeight() {
        return weight;
    }

    public ElasticQueryBuilder setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public ElasticQueryBuilder getQuery() {
        return query;
    }

    public ElasticQueryBuilder setQuery(ElasticQueryBuilder query) {
        this.query = query;
        return this;
    }

    private void buildIncludedFields(ObjectNode node) {
        ArrayNode arrayFields = mapper.valueToTree(this.includeFields);
        node.putArray("includes").addAll(arrayFields);
    }
    private void buildSort(ObjectNode node) {
        if (!this.sort.isEmpty()) {
            List<ObjectNode> orderFields = new LinkedList<>();
            Map<String, ObjectNode> order = new LinkedHashMap<>();
            this.sort.entrySet().forEach((e) -> order.put(e.getKey(), mapper.createObjectNode().put("order", e.getValue())));
            this.sort.entrySet().forEach((e) -> orderFields.add((ObjectNode) mapper.createObjectNode().set(e.getKey(), order.get(e.getKey()))));
            JsonNode sortProperties = mapper.valueToTree(orderFields);
            node.set("sort", sortProperties);
        } else throw new IllegalArgumentException("Sort properties are not set, or have invalid format.");

    }

    private void buildFilters(ObjectNode node) {
        ArrayNode filterArray= mapper.createArrayNode();

        if (this.filterRange != null && !this.filterRange.isEmpty()) {

            for(Map.Entry<String,Object> entry:this.filterRange.entrySet()){
                ObjectNode filterRangeProperties = mapper.createObjectNode();
                ObjectNode filterRange = mapper.createObjectNode();
                ObjectNode filterOuter = mapper.createObjectNode();
                Map<String,String> innerRange = mapper.convertValue(entry.getValue(),Map.class);
                for(Map.Entry<String,String> range:innerRange.entrySet()){
                    filterRangeProperties.put(range.getKey(),range.getValue());
                    filterRange.set(entry.getKey(),filterRangeProperties);
                    filterOuter.set("range",filterRange);
                }

                filterArray.add(filterOuter);
            }

        }
        if (this.filterTerms != null && !this.filterTerms.isEmpty()) {

            for(Map.Entry<String,String> entry:this.filterTerms.entrySet()){
                ObjectNode filterTermProperties = mapper.createObjectNode();
                ObjectNode filterTerm = mapper.createObjectNode();
                filterTermProperties.put(entry.getKey(),entry.getValue());
                filterTerm.set("term",filterTermProperties);
                filterArray.add(filterTerm);
            }

        }
        node.withArray("filter").addAll(filterArray);
    }



    private void buildRange(ArrayNode node) {
        if (!this.range.isEmpty() && !this.rangeField.isEmpty()) {
            ObjectNode objectRange = mapper.createObjectNode();
            JsonNode rangeFields = mapper.convertValue(this.range, JsonNode.class);
            ObjectNode rangeField = mapper.createObjectNode();
            rangeField.set(this.rangeField, rangeFields);
            objectRange.set("range", rangeField);
            node.add(objectRange);
        } else throw new IllegalArgumentException("Range properties or field are not set, or have invalid format.");
    }



    private void buildFunctions(ObjectNode node){
        ObjectNode functionScore = mapper.createObjectNode();
        ObjectNode boost = mapper.createObjectNode();
        ArrayNode functions = mapper.createArrayNode();

        for(Map.Entry<String,Integer> term:termsWithWeight.entrySet()){
            ObjectNode function = mapper.createObjectNode();
            ObjectNode filter = mapper.createObjectNode();
            ObjectNode termNode = mapper.createObjectNode();
            ObjectNode match = mapper.createObjectNode();
            termNode.put(AppConfig.searchField,term.getKey());

            match.set("match",termNode);
            function.set("filter",match);
            function.put("weight", String.valueOf(term.getValue()));
            functions.add(function);
        }
        boost.put("boost",this.boost);
        functionScore.setAll(boost);
        functionScore.withArray("functions").addAll(functions);
        node.set("function_score",functionScore);
    }


    public String build() throws IOException {

        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode objectQuery = mapper.createArrayNode();
        ObjectNode queryStringField = mapper.createObjectNode();
        ObjectNode query = mapper.createObjectNode();
        ObjectNode boolQueryNode = mapper.createObjectNode();
        ObjectNode boolQueryContent = mapper.createObjectNode();
        ObjectNode sourceFiltering = mapper.createObjectNode();
        // System.out.println(this.boolQuery);

        if (this.sort != null) {
            //set to root node
            buildSort(rootNode);
        }

        ObjectNode scoreField = mapper.createObjectNode().put("min_score", this.minScore);
        rootNode.setAll(scoreField);


        if (this.includeFields != null) {
            buildIncludedFields(sourceFiltering);
        }
        if (this.range != null) {
            //Add range to "query"
            buildRange(objectQuery);
        }

        if (sourceFiltering.has("includes") || sourceFiltering.has("excludes")) {
            rootNode.set("_source", sourceFiltering);
        }

        if(termsWithWeight != null){
            buildFunctions(rootNode);
        }

        if (this.filterRange != null || this.filterTerms != null) {
            //Add content to boolQueryContent
            buildFilters(boolQueryContent);
        }

        //Wrap content of boolQueryContent to create a "bool" query
        boolQueryNode.set("bool", boolQueryContent);

        query.set("query", boolQueryNode);
        //wrap whole query to root/ used a pseudo root  element to add external nodes without a parent. eg.size,from.
        rootNode.setAll(query);


        return mapper.writeValueAsString(rootNode);
    }
}
