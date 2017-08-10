package org.devocative.ares.service.terminal;

public interface ITerminalProcess {
	long getConnectionId();

	void send(Object message);

	void close();

	long getLastActivityTime();
}
