package org.devocative.ares.vo;

import java.io.Serializable;
import java.util.*;

public class TabularVO<T> implements Serializable {
	private static final long serialVersionUID = 5985989025821421183L;

	private final List<String> columns;
	private final List<Map<String, T>> data = new ArrayList<>();

	// ------------------------------

	public TabularVO(List<String> columns, List<List<T>> rows) {
		this.columns = columns;

		for (List<T> row : rows) {
			Map<String, T> map = new LinkedHashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				map.put(columns.get(i), row.get(i));
			}
			data.add(map);
		}
	}

	// ------------------------------

	public Collection<String> getColumns() {
		return data.isEmpty() ? columns : data.get(0).keySet();
	}

	public List<Map<String, T>> getRows() {
		return data;
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("COLS: %s\nROWS: %s\n", columns, data);
	}
}
