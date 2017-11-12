package org.devocative.ares.vo;

import java.io.Serializable;
import java.util.*;

public class TabularVO<T> implements Serializable {
	private static final long serialVersionUID = 5985989025821421183L;

	private final List<String> columns;
	private final List<Map<String, T>> listOfDataAsMap = new ArrayList<>();
	private final Map<String, T> filter;

	private Integer size;

	// ------------------------------

	public TabularVO(List<String> columns, List<List<T>> rows) {
		this(columns, rows, null);
	}

	public TabularVO(List<String> columns, List<List<T>> rows, Map<String, T> filter) {
		this.columns = columns;
		this.filter = filter;

		for (List<T> row : rows) {
			Map<String, T> map = new LinkedHashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				map.put(columns.get(i), row.get(i));
			}

			if (filter == null || filter.size() == 0 || isPassed(map, filter)) {
				listOfDataAsMap.add(map);
			}
		}
	}

	// ------------------------------

	public Collection<String> getColumns() {
		return listOfDataAsMap.isEmpty() ? columns : listOfDataAsMap.get(0).keySet();
	}

	public List<Map<String, T>> getRows() {
		return listOfDataAsMap;
	}

	public Map<String, T> getObject() {
		if (listOfDataAsMap.size() == 0) {
			throw new RuntimeException("No result found, filters = " + filter);
		} else if (listOfDataAsMap.size() == 1) {
			return listOfDataAsMap.get(0);
		} else {
			throw new RuntimeException("More than one result found: size = " + listOfDataAsMap.size() + ", filters = " + filter);
		}
	}

	public Integer getSize() {
		return size != null ? size : listOfDataAsMap.size();
	}

	public TabularVO<T> setSize(Integer size) {
		this.size = size;
		return this;
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("COLS: %s\nROWS: %s\n", columns, listOfDataAsMap);
	}

	// ------------------------------

	private boolean isPassed(Map<String, T> map, Map<String, T> filter) {
		boolean allOk = true;

		for (Map.Entry<String, T> entry : filter.entrySet()) {
			if (entry.getValue() != null && !entry.getValue().equals(map.get(entry.getKey()))) {
				allOk = false;
				break;
			}
		}

		return allOk;
	}
}
