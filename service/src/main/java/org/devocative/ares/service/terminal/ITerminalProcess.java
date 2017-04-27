package org.devocative.ares.service.terminal;

public interface ITerminalProcess {
	void send(String text, Integer specialKey);

	void close();
}
