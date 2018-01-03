package org.devocative.ares.service.terminal;

public interface ITerminalProcess {
	long getConnectionId();

	void send(Object message);

	void cancel() throws Exception;

	long getLastActivityTime();

	boolean isBusy();
}
