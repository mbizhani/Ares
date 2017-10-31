//overwrite
package org.devocative.ares.vo.filter.command;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class PrepCommandFVO implements Serializable {
	private static final long serialVersionUID = 1119320524L;

	private String name;
	private String code;
	private String params;
	private Boolean enabled;
	private List<Command> command;
	private List<OServiceInstance> serviceInstance;
	private List<User> allowedUsers;
	private List<Role> allowedRoles;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Command> getCommand() {
		return command;
	}

	public void setCommand(List<Command> command) {
		this.command = command;
	}

	public List<OServiceInstance> getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(List<OServiceInstance> serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	public List<User> getAllowedUsers() {
		return allowedUsers;
	}

	public void setAllowedUsers(List<User> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}

	public List<Role> getAllowedRoles() {
		return allowedRoles;
	}

	public void setAllowedRoles(List<Role> allowedRoles) {
		this.allowedRoles = allowedRoles;
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