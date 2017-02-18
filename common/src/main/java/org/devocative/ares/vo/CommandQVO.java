package org.devocative.ares.vo;

import org.devocative.ares.entity.oservice.OServiceInstance;

import java.io.Serializable;
import java.util.Map;

public class CommandQVO implements Serializable {
	private static final long serialVersionUID = 3413442591805727155L;

	private Long commandId;
	private OServiceInstance serviceInstance;
	private Map<String, String> params;

	public CommandQVO(Long commandId, OServiceInstance serviceInstance, Map<String, String> params) {
		this.commandId = commandId;
		this.serviceInstance = serviceInstance;
		this.params = params;
	}

	public Long getCommandId() {
		return commandId;
	}

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
