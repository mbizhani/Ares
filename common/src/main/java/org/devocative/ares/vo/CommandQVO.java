package org.devocative.ares.vo;

import java.io.Serializable;
import java.util.Map;

public class CommandQVO implements Serializable {
	private static final long serialVersionUID = 3413442591805727155L;

	private Long commandId;
	private String commandName;
	private Long serviceInstanceId;
	private Map<String, Object> params;
	private Long osiUserId;

	private Long prepCommandId;

	// ------------------------------

	public CommandQVO(Long commandId, Long serviceInstanceId, Map<String, Object> params, Long prepCommandId) {
		this.commandId = commandId;
		this.serviceInstanceId = serviceInstanceId;
		this.params = params;

		this.prepCommandId = prepCommandId;
	}

	public CommandQVO(String commandName, Long serviceInstanceId, Map<String, Object> params) {
		this.commandName = commandName;
		this.serviceInstanceId = serviceInstanceId;
		this.params = params;
	}

	// ------------------------------

	public Long getCommandId() {
		return commandId;
	}

	public String getCommandName() {
		return commandName;
	}

	public Long getServiceInstanceId() {
		return serviceInstanceId;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public Long getPrepCommandId() {
		return prepCommandId;
	}

	// ---------------

	public Long getOsiUserId() {
		return osiUserId;
	}

	public CommandQVO setOsiUserId(Long osiUserId) {
		this.osiUserId = osiUserId;
		return this;
	}
}
