package org.devocative.ares.cmd;

import org.devocative.ares.vo.TabularVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConsoleResultProcessing {
	private static final Logger logger = LoggerFactory.getLogger(ConsoleResultProcessing.class);

	private int ignoreStartingLines = 0;

	private int ignoreLinesAfterHeader = 0;

	private List<String> columns = new ArrayList<>();

	private List<List<String>> rows = new ArrayList<>();

	private String text;

	private Integer size;

	// ---------------

	private List<String> possibleColumns = new ArrayList<>();

	private String splitBy;

	// ---------------

	private List<Integer> indexOfColumns = new ArrayList<>();

	// ------------------------------

	public ConsoleResultProcessing(String text) {
		this.text = text;
	}

	// ------------------------------

	public ConsoleResultProcessing setIgnoreStartingLines(int ignoreStartingLines) {
		this.ignoreStartingLines = ignoreStartingLines;
		return this;
	}

	public ConsoleResultProcessing setIgnoreLinesAfterHeader(int ignoreLinesAfterHeader) {
		this.ignoreLinesAfterHeader = ignoreLinesAfterHeader;
		return this;
	}

	public ConsoleResultProcessing setPossibleColumns(String... columns) {
		copyArrToCollection(columns, this.possibleColumns);
		return this;
	}

	public ConsoleResultProcessing setSplitBy(String splitBy) {
		this.splitBy = splitBy;
		return this;
	}

	public ConsoleResultProcessing setSize(Integer size) {
		this.size = size;
		return this;
	}

	public List<String> getColumns() {
		return columns;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	// ---------------

	public ConsoleResultProcessing prepend(String txt) {
		text = txt + text;
		return this;
	}

	public ConsoleResultProcessing append(String txt) {
		text = text + txt;
		return this;
	}

	public TabularVO build() {
		return build(null);
	}

	public TabularVO build(Map<String, String> filter) {
		String[] split = text.split("[\n]");

		List<String> lines = new ArrayList<>();
		copyArrToCollection(split, lines);

		for (int i = 0; i < ignoreStartingLines; i++) {
			lines.remove(0);
		}

		for (int i = 0; i < ignoreLinesAfterHeader; i++) {
			lines.remove(1);
		}

		String header = lines.remove(0);

		if (!possibleColumns.isEmpty()) {
			findByColumnNames(header);
			fillRowsByIndex(lines);
		} else if (splitBy != null) {
			fillBySplit(header, lines);
		} else {
			throw new RuntimeException("Auto-find is not implemented!");
		}

		logger.debug("Final Columns: {}", columns);
		logger.debug("Rows: {}", rows);

		return new TabularVO<>(columns, rows, filter).setSize(size);
	}

	// ------------------------------

	private void findByColumnNames(String header) {
		indexOfColumns.add(0);

		for (String column : possibleColumns) {
			int idx = 0;
			while (idx > -1) {
				idx = header.indexOf(column, idx);
				if (idx > -1) {
					if (!indexOfColumns.contains(idx)) {
						indexOfColumns.add(idx);
					}

					idx += column.length();
				}
			}
		}

		Collections.sort(indexOfColumns);

		String firstCol = header.substring(indexOfColumns.get(0), indexOfColumns.get(1));
		if (firstCol.trim().isEmpty()) {
			indexOfColumns.remove(1);
		}
		logger.debug("Found Index of Columns: {}", indexOfColumns);

		findPartsByIndex(header, columns);
	}

	private void fillRowsByIndex(List<String> lines) {
		for (String line : lines) {
			List<String> cells = new ArrayList<>();

			findPartsByIndex(line, cells);

			rows.add(cells);
		}
	}

	private void findPartsByIndex(String line, List<String> cells) {
		for (int i = 0; i < indexOfColumns.size(); i++) {
			int start = indexOfColumns.get(i);
			int end;
			if ((i + 1) < indexOfColumns.size()) {
				end = indexOfColumns.get(i + 1);
			} else {
				end = line.length();
			}

			String cell = line.substring(start, end).trim();
			cells.add(cell);
		}
	}

	private void fillBySplit(String header, List<String> lines) {
		String[] headerParts = header.split(splitBy);
		copyArrToCollection(headerParts, columns);

		for (String line : lines) {
			String[] lineParts = line.split(splitBy);
			List<String> cells = new ArrayList<>();
			copyArrToCollection(lineParts, cells);

			if (lineParts.length < headerParts.length) {
				int diff = headerParts.length - lineParts.length;
				for (int i = 0; i < diff; i++) {
					cells.add("");
				}
			}

			rows.add(cells);
		}
	}

	private void copyArrToCollection(String[] arr, Collection<String> col) {
		for (String cell : arr) {
			col.add(cell.trim());
		}
	}
}
