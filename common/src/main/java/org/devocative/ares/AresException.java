package org.devocative.ares;

import org.devocative.demeter.imodule.DModuleException;


public class AresException extends DModuleException {
	private static final long serialVersionUID = 1L;

	public AresException(AresErrorCode errorCode) {
		this(errorCode, null, null);
	}

	public AresException(AresErrorCode errorCode, String errorParameter) {
		this(errorCode, errorParameter, null);
	}

	public AresException(AresErrorCode errorCode, Throwable cause) {
		this(errorCode, null, cause);
	}

	// Main Constructor
	public AresException(AresErrorCode errorCode, String errorParameter, Throwable cause) {
		super("ARS", errorCode, errorParameter, cause);
	}
}
