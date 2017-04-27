package org.devocative.ares.web;

import java.io.Serializable;

public class TerminalTabInfo implements Serializable {
	private static final long serialVersionUID = 9036604255791901695L;

	private String tabId;
	private Long connectionId;

	public TerminalTabInfo(String tabId, Long connectionId) {
		this.tabId = tabId;
		this.connectionId = connectionId;
	}

	public String getTabId() {
		return tabId;
	}

	public Long getConnectionId() {
		return connectionId;
	}

	@Override
	public String toString() {
		return String.format("tabId=%s, connectionId=%s", tabId, connectionId);
	}
}

