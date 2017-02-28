package org.devocative.ares.vo;

import java.io.Serializable;
import java.util.List;

public class TabularVO implements Serializable {
	private static final long serialVersionUID = 5985989025821421183L;

	private List<String> columns;
	private List<List<String>> rows;

	// ------------------------------

	public TabularVO(List<String> columns, List<List<String>> rows) {
		this.columns = columns;
		this.rows = rows;
	}

	// ------------------------------

	public List<String> getColumns() {
		return columns;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("COLS: %s\nROWS: %s\n", columns, rows);
	}
}
