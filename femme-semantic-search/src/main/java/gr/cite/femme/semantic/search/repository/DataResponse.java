package gr.cite.femme.semantic.search.repository;

import java.util.List;
import java.util.Map;

public class DataResponse<T> {

    private List<T> records;

    private List<Map<String,Object>> info;

    private int totalElements;
    
    private String scrollId;

    public int getTotalRecords() {
        return totalElements;
    }

    public void setTotalRecords(int totalElements) {
        this.totalElements = totalElements;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public List<Map<String, Object>> getInfo() {
        return info;
    }

    public void setInfo(List<Map<String, Object>> info) {
        this.info = info;
    }

	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}


}
