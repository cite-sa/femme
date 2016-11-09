package gr.cite.femme.query.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_EMPTY)
public class QueryOptionsFields {
	
	@JsonProperty
	private Integer limit;
	
	@JsonProperty
	private Integer offset;
	
	@JsonProperty
	private String asc;
	
	@JsonProperty
	private String desc;
	
	@JsonProperty
	private HashSet<String> include;
	
	@JsonProperty
	private HashSet<String> exclude;

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getAsc() {
		return asc;
	}

	public void setAsc(String asc) {
		this.asc = asc;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public HashSet<String> getInclude() {
		return include;
	}

	public void setInclude(HashSet<String> include) {
		this.include = include;
	}

	public HashSet<String> getExclude() {
		return exclude;
	}

	public void setExclude(HashSet<String> exclude) {
		this.exclude = exclude;
	}
	
	public static QueryOptionsFields valueOf(String optionsJson) throws JsonParseException, JsonMappingException, IOException {
		if (optionsJson != null) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(optionsJson, QueryOptionsFields.class);
		}
		return null;
	}
	
	
}
