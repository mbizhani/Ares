package org.devocative.ares.service.terminal;

public interface ITerminalProcess {
	long getConnectionId();

	void send(String text, Integer specialKey);

	void close();

	long getLastActivityTime();
}
