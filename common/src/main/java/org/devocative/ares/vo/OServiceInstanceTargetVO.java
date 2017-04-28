package org.devocative.ares.vo;

import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OServiceInstance;

import java.io.Serializable;
import java.util.Map;

public class OServiceInstanceTargetVO implements Serializable {
	private static final long serialVersionUID = -3971547424645822180L;

	private String connection;
	private Map<String, String> prop;
	private String password;
	private boolean sudoer = false;

	private OSIUser user;
	private OServiceInstance serviceInstance;

	// ------------------------------

	public OServiceInstanceTargetVO(OServiceInstance serviceInstance, OSIUser user, String password, Map<String, String> prop) {
		this.serviceInstance = serviceInstance;
		this.user = user;
		this.password = password;
		this.prop = prop;
	}

	// ------------------------------

	public Long getId() {
		return serviceInstance.getId();
	}

	public String getName() {
		return serviceInstance.toString();
	}

	public String getAddress() {
		return serviceInstance.getServer().getAddress();
	}

	public Integer getPort() {
		return serviceInstance.getPortSafely();
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String getUsername() {
		return user.getUsername();
	}

	public String getPassword() {
		return password;
	}

	public Map<String, String> getProp() {
		return prop;
	}

	public boolean isSudoer() {
		return sudoer;
	}

	public OServiceInstanceTargetVO setSudoer(boolean sudoer) {
		this.sudoer = sudoer;
		return this;
	}

	// ---------------

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public OSIUser getUser() {
		return user;
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("srvInst=%s osiUser=%s", serviceInstance, getUsername());
	}
}
