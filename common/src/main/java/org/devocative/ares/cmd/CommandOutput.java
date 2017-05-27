package org.devocative.ares.cmd;

import java.io.Serializable;

public class CommandOutput implements Serializable {
	private static final long serialVersionUID = -4930950993675996103L;

	private Type type;
	private Object output;

	// ------------------------------

	public CommandOutput(Object output) {
		this(Type.LINE, output);
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
		PROMPT,
		LINE,
		TABULAR,
		ERROR
	}
}
