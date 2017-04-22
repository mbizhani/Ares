package org.devocative.ares.cmd;

import java.io.Serializable;

public class CommandOutput implements Serializable {
	private static final long serialVersionUID = -4930950993675996103L;

	private Type type;
	private String line;

	// ------------------------------

	public CommandOutput(String line) {
		this(Type.LINE, line);
	}

	public CommandOutput(Type type, String line) {
		this.type = type;
		this.line = line;
	}

	// ------------------------------

	public Type getType() {
		return type;
	}

	public String getLine() {
		return line;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", getType(), getLine());
	}

	// ------------------------------

	public enum Type {
		PROMPT,
		LINE,
		ERROR
	}
}
