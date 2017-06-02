package org.devocative.ares.cmd;

import java.util.Map;

public class SshResult {
	private String stdout;
	private int exitStatus;

	public SshResult(String stdout, int exitStatus) {
		this.stdout = stdout;
		this.exitStatus = exitStatus;
	}

	public String getStdout() {
		return stdout;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	// ---------------

	public ConsoleResultProcessing toTabular(String splitBy) {
		return new ConsoleResultProcessing(stdout).setSplitBy(splitBy);
	}

	public ConsoleResultProcessing toTabular(String[] columns) {
		return new ConsoleResultProcessing(stdout).setPossibleColumns(columns);
	}

	public ConsoleResultProcessing toTabular() {
		return new ConsoleResultProcessing(stdout);
	}

	public Map<String, String> toMap() {
		return toMap("[\n]", "[=]");
	}

	public Map<String, String> toMap(String lineDelimiter, String keyValueDelimiter) {
		Map<String, String> result = new AresMap<>();

		String[] lines = stdout.split(lineDelimiter);
		for (String line : lines) {
			String[] keyValue = line.split(keyValueDelimiter);
			String key = keyValue[0].trim();
			String value = keyValue[1].trim();
			if (value.startsWith("\"")) {
				value = value.substring(1);
			}

			if (value.endsWith("\"")) {
				value = value.substring(0, value.length() - 1);
			}

			result.put(key, value);
		}
		return result;
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("Exit Status = %s", exitStatus);
	}
}
