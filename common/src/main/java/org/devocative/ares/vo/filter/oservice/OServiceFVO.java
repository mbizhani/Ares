//overwrite
package org.devocative.ares.vo.filter.oservice;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class OServiceFVO implements Serializable {
	private static final long serialVersionUID = 1864586265L;

	private String name;
	private String connectionPattern;
	private RangeVO<Integer> adminPort;
	private String ports;
	private List<OServiceProperty> properties;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnectionPattern() {
		return connectionPattern;
	}

	public void setConnectionPattern(String connectionPattern) {
		this.connectionPattern = connectionPattern;
	}

	public RangeVO<Integer> getAdminPort() {
		return adminPort;
	}

	public void setAdminPort(RangeVO<Integer> adminPort) {
		this.adminPort = adminPort;
	}

	public String getPorts() {
		return ports;
	}

	public void setPorts(String ports) {
		this.ports = ports;
	}

	public List<OServiceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<OServiceProperty> properties) {
		this.properties = properties;
	}

	public RangeVO<Date> getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(RangeVO<Date> creationDate) {
		this.creationDate = creationDate;
	}

	public List<User> getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(List<User> creatorUser) {
		this.creatorUser = creatorUser;
	}

	public RangeVO<Date> getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(RangeVO<Date> modificationDate) {
		this.modificationDate = modificationDate;
	}

	public List<User> getModifierUser() {
		return modifierUser;
	}

	public void setModifierUser(List<User> modifierUser) {
		this.modifierUser = modifierUser;
	}

}