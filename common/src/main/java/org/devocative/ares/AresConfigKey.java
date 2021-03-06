package org.devocative.ares;

import org.devocative.adroit.IConfigKey;

import java.util.Arrays;
import java.util.List;

public enum AresConfigKey implements IConfigKey {
	ShellResponseResetExpiration("ars.shell.response.reset.expiration", false, Arrays.asList(true, false)),
	GeneralCommandExecLimit("ars.cmd.limit", 15),
	ConsiderInnerCommandForLimit("ars.cmd.limit.inner", true, Arrays.asList(true, false)),
	SIUserUsernameRegEx("ars.siuser.username.regex", "^[A-Za-z]+?[A-Za-z0-9.]*?$"),

	SendOutDelayedEnabled("ars.out.delayed.enabled", true, Arrays.asList(true, false)),
	SendOutDelayedDuration("ars.out.delayed.duration", 50),
	SendOutDelayedExceededSize("ars.out.exceeded.size", 1100),
	SendOutDelayedExceededOmit("ars.out.exceeded.omit", 100),
	SendOutDelayedExceededOmitSkip("ars.out.exceeded.omit.skip", 5)
	;
	// ------------------------------

	private String key;
	private boolean validate = false;
	private Object defaultValue;
	private List<?> possibilities;

	// ------------------------------

	AresConfigKey(String key) {
		this(false, key, null);
	}

	AresConfigKey(String key, List<?> possibilities) {
		this(false, key, possibilities);
	}

	AresConfigKey(boolean validate, String key) {
		this(validate, key, null);
	}

	// Main Constructor 1
	AresConfigKey(boolean validate, String key, List<?> possibilities) {
		this.key = key;
		this.validate = validate;
		this.possibilities = possibilities;
	}

	// ---------------

	AresConfigKey(String key, Object defaultValue) {
		this(key, defaultValue, null);
	}

	// Main Constructor 2
	AresConfigKey(String key, Object defaultValue, List<?> possibilities) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.possibilities = possibilities;
	}

	// ------------------------------

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public boolean getValidate() {
		return validate;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public List<?> getPossibleValues() {
		return possibilities;
	}
}
