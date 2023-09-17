package com.exist.model7r1;

import java.util.List;
import java.util.Map;

public class Table {
	private String filePath;
	private Map<String, List<String>> keyValueMap;
	private List<Integer> dimension;
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public Map<String, List<String>> getKeyValueMap() {
		return keyValueMap;
	}

	public void setKeyValueMap(Map<String, List<String>> keyValueMap) {
		this.keyValueMap = keyValueMap;
	}
	
	public List<Integer> getDimension() {
		return dimension;
	}
	
	public void setDimension(List<Integer> dimension) {
		this.dimension = dimension;
	}
}
