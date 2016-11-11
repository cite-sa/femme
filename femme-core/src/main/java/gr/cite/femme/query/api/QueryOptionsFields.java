package gr.cite.femme.query.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
	private Set<String> include;
	
	@JsonProperty
	private Set<String> exclude;

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

	public Set<String> getInclude() {
		return include;
	}

	public void setInclude(Set<String> include) {
		this.include = include;
	}

	public Set<String> getExclude() {
		return exclude;
	}

	public void setExclude(Set<String> exclude) {
		this.exclude = exclude;
	}
	
	public static QueryOptionsFields valueOf(String optionsJson) throws JsonParseException, JsonMappingException, IOException {
		if (optionsJson != null) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(optionsJson, QueryOptionsFields.class);
		}
		return null;
	}
	
	public static Builder builder() {
		return new QueryOptionsFields.Builder();
	}
	
	public static class Builder {
		
		private QueryOptionsFields options;
		
		private Builder() {
			this.options = new QueryOptionsFields();
		}
		
		public Builder limit(Integer limit) {
			options.setLimit(limit);
			return this;
		}
		
		public Builder offset(Integer offset) {
			options.setOffset(offset);
			return this;
		}
		
		public Builder asc(String asc) {
			options.setAsc(asc);
			return this;
		}
		
		public Builder desc(String desc) {
			options.setDesc(desc);
			return this;
		}
		
		public Builder include(Set<String> include) {
			options.setInclude(include);
			return this;
		}
		
		public Builder exclude(Set<String> exclude) {
			options.setExclude(exclude);
			return this;
		}
		
		public QueryOptionsFields build() {
			return this.options;
		}
	}
	
}
