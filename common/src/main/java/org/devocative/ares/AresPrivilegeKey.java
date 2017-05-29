package org.devocative.ares;

import org.devocative.demeter.entity.IPrivilegeKey;

public enum AresPrivilegeKey implements IPrivilegeKey {

	OServiceAdd, OServiceEdit,
	OServicePropertyAdd, OServicePropertyEdit,
	OServiceInstanceAdd, OServiceInstanceEdit,
	OSIUserAdd, OSIUserEdit, OSIUserShowPassword,
	OServerAdd, OServerEdit,
	OBasicDataAdd, OBasicDataEdit;

	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setModule(String module) {
		name = String.format("%s.%s", module, name());
	}
}
