package org.devocative.ares.vo;

import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OServiceInstance;

import java.io.Serializable;
import java.util.Map;

public class OServiceInstanceTargetVO implements Serializable {
	private static final long serialVersionUID = -3971547424645822180L;

	private String connection;
	private Map<String, String> prop;
	private OServiceInstance serviceInstance;
	private OSIUser user;

	// ------------------------------

	public OServiceInstanceTargetVO(OServiceInstance serviceInstance, OSIUser user, Map<String, String> prop) {
		this.serviceInstance = serviceInstance;
		this.user = user;
		this.prop = prop;
	}

	// ------------------------------

	public Long getId() {
		return serviceInstance.getId();
	}

	public String getName() {
		return serviceInstance.getName();
	}

	public String getFullName() {
		return String.format("%s[%s]@%s",
			serviceInstance.getName(),
			serviceInstance.getService().getName(),
			serviceInstance.getServer().getName());
	}

	public String getAddress() {
		return serviceInstance.getServer().getAddress();
	}

	public Integer getPort() {
		return serviceInstance.getPort();
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public OSIUser getUser() {
		return user;
	}

	public Map<String, String> getProp() {
		return prop;
	}
}
