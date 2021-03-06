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

	@Deprecated
	public Map<String, T> getObject() {
		return getSingleRow();
	}

	public Map<String, T> getSingleRow() {
		if (listOfDataAsMap.size() == 0) {
			throw new RuntimeException("No result found, filters = " + filter);
		} else if (listOfDataAsMap.size() == 1) {
			return listOfDataAsMap.get(0);
		} else {
			throw new RuntimeException("More than one result found: size = " + listOfDataAsMap.size() + ", filters = " + filter);
		}
	}

	public T getSingleCell() {
		if (listOfDataAsMap.size() == 0) {
			throw new RuntimeException("No result found, filters = " + filter);
		} else if (listOfDataAsMap.size() == 1 && listOfDataAsMap.get(0).size() == 1) {
			return listOfDataAsMap.get(0)
				.values()
				.iterator()
				.next();
		} else {
			throw new RuntimeException("More than one row/cell found: size = " + listOfDataAsMap.size() + ", filters = " + filter);
		}
	}


	public Map<String, T> getFirstRow() {
		if (listOfDataAsMap.size() == 0) {
			throw new RuntimeException("No result found, filters = " + filter);
		} else {
			return listOfDataAsMap.get(0);
		}
	}

	public T getFirstCell() {
		if (listOfDataAsMap.size() > 0) {
			return listOfDataAsMap.get(0)
				.values()
				.iterator()
				.next();
		} else {
			throw new RuntimeException("No result found, filters = " + filter);
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

	private boolean isPassed(Map<String, T> row, Map<String, T> filter) {
		boolean allOk = true;

		for (Map.Entry<String, T> entry : filter.entrySet()) {
			T filterValue = entry.getValue();
			T cellValue = row.get(entry.getKey());
			if (filterValue != null &&
				cellValue != null &&
				!filterValue.equals(cellValue) &&
				!cellValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase())
				) {
				allOk = false;
				break;
			}
		}

		return allOk;
	}
}
