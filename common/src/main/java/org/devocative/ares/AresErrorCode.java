package org.devocative.ares;

import org.devocative.demeter.imodule.DErrorCode;

public enum AresErrorCode implements DErrorCode {
	DuplicateExecutor("Duplicate Executor"),
	CommandNotFound("Command not found")
	//SQLExecution("SQL Execution"),
	;

	// ------------------------------

	private String defaultDescription;

	// ------------------------------

	AresErrorCode(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	// ------------------------------

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getDefaultDescription() {
		return defaultDescription;
	}
}
