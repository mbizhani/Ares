package org.devocative.ares.cmd;

import java.io.Serializable;

public class CommandOutput implements Serializable {
	private static final long serialVersionUID = -4930950993675996103L;

	private Type type;
	private Object output;

	// ------------------------------

	public CommandOutput(Type type) {
		this(type, null);
	}

	public CommandOutput(Type type, Object output) {
		this.type = type;
		this.output = output;
	}

	// ------------------------------

	public Type getType() {
		return type;
	}

	public Object getOutput() {
		return output != null ? output : "-";
	}

	@Override
	public String toString() {
		return String.format("%s:%s", getType(), getOutput());
	}

	// ------------------------------

	public enum Type {
		START,
		PROMPT,
		LINE,
		TABULAR,
		ERROR,
		WARN,
		FINISHED
	}
}
