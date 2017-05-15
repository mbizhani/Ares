package org.devocative.ares.vo;

import java.io.Serializable;
import java.util.*;

public class TabularVO<T> implements Serializable {
	private static final long serialVersionUID = 5985989025821421183L;

	private final List<String> columns;
	private final List<Map<String, T>> data = new ArrayList<>();

	// ------------------------------

	public TabularVO(List<String> columns, List<List<T>> rows) {
		this(columns, rows, null);
	}

	public TabularVO(List<String> columns, List<List<T>> rows, Map<String, T>[] filters) {
		this.columns = columns;

		for (List<T> row : rows) {
			Map<String, T> map = new LinkedHashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				map.put(columns.get(i), row.get(i));
			}

			if (filters == null || filters.length == 0 || isPassed(map, filters)) {
				data.add(map);
			}
		}
	}

	// ------------------------------

	public Collection<String> getColumns() {
		return data.isEmpty() ? columns : data.get(0).keySet();
	}

	public List<Map<String, T>> getRows() {
		return data;
	}

	public Map<String, T> getObject() {
		if (data.size() == 1) {
			return data.get(0);
		} else {
			throw new RuntimeException("Invalid tabular single-result: size = " + data.size());
		}
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("COLS: %s\nROWS: %s\n", columns, data);
	}

	// ------------------------------

	private boolean isPassed(Map<String, T> map, Map<String, T>[] filters) {
		for (Map<String, T> filter : filters) {
			boolean allOk = true;

			for (Map.Entry<String, T> entry : filter.entrySet()) {
				if (!entry.getValue().equals(map.get(entry.getKey()))) {
					allOk = false;
					break;
				}
			}

			if (allOk) {
				return true;
			}
		}
		return false;
	}
}
