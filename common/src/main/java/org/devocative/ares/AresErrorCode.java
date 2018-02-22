package org.devocative.ares;

import org.devocative.demeter.imodule.DErrorCode;

public enum AresErrorCode implements DErrorCode {
	DuplicateExecutor("Duplicate Executor"),
	CommandNotFound("Command not found"),
	TerminalConnectionAccessViolation("Terminal Connection Access Violation"),
	ExecutorUserNotFound("Executor User Not Found"),
	DuplicateServiceInstance("Duplicate Service Instance"),
	DuplicateUsername("Duplicate Service Instance's username"),
	CommandExecLimitViolation("Command Exec Limit Violation for Service Instance"),
	InvalidServiceInstanceUsername("Invalid username for service instance")
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
