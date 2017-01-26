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
}
