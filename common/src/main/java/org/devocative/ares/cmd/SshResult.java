package org.devocative.ares.cmd;

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

	// ---------------

	@Override
	public String toString() {
		return String.format("Exit Status = %s", exitStatus);
	}
}
