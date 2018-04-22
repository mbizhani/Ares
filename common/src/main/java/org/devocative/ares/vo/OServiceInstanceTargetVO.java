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

	private OSIUser admin;
	private String adminPassword;

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

	public OSIUser getAdmin() {
		return admin;
	}

	public OServiceInstanceTargetVO setAdmin(OSIUser admin) {
		this.admin = admin;
		return this;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public OServiceInstanceTargetVO setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		return this;
	}

	// ---------------

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public OSIUser getUser() {
		return user;
	}

	public Long getServerId() {
		return serviceInstance.getServerIdSafely();
	}

	// ---------------

	@Override
	public String toString() {
		return String.format("srvInst=%s osiUser=%s", serviceInstance, getUsername());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OServiceInstanceTargetVO)) return false;

		OServiceInstanceTargetVO targetVO = (OServiceInstanceTargetVO) o;

		return !(getUser() != null ? !getUser().equals(targetVO.getUser()) : targetVO.getUser() != null);

	}

	@Override
	public int hashCode() {
		return getUser() != null ? getUser().hashCode() : 0;
	}
}
