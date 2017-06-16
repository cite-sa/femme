package gr.cite.femme.core.dto;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_EMPTY)
public class QueryOptionsMessenger {
	
	@JsonProperty("limit")
	private Integer limit;
	
	@JsonProperty("offset")
	private Integer offset;

	// TODO implement orderBy
	@JsonProperty("orderBy")
	private List<String> orderBy;
	
	@JsonProperty("asc")
	private String asc;
	
	@JsonProperty("desc")
	private String desc;
	
	@JsonProperty("include")
	private Set<String> include;
	
	@JsonProperty("exclude")
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

	public List<String> getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(List<String> orderBy) {
		this.orderBy = orderBy;
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
	
	public static QueryOptionsMessenger valueOf(String optionsJson) throws JsonParseException, JsonMappingException, IOException {
		if (optionsJson != null) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(optionsJson, QueryOptionsMessenger.class);
		}
		return null;
	}
	
	public static Builder builder() {
		return new QueryOptionsMessenger.Builder();
	}
	
	public static class Builder {
		
		private QueryOptionsMessenger options;
		
		private Builder() {
			this.options = new QueryOptionsMessenger();
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

		public Builder include(String... include) {
			options.setInclude(new HashSet<>(Arrays.asList(include)));
			return this;
		}

		public Builder include(Set<String> include) {
			options.setInclude(include);
			return this;
		}

		public Builder exclude(String... exclude) {
			options.setExclude(new HashSet<>(Arrays.asList(exclude)));
			return this;
		}
		
		public Builder exclude(Set<String> exclude) {
			options.setExclude(exclude);
			return this;
		}
		
		public QueryOptionsMessenger build() {
			return this.options;
		}
	}
	
}
