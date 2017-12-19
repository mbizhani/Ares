//overwrite
package org.devocative.ares.vo.filter.oservice;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class OServiceInstanceFVO implements Serializable {
	private static final long serialVersionUID = -385193660L;

	private RangeVO<Integer> port;
	private List<OServer> server;
	private List<OService> service;
	private List<User> allowedUsers;
	private List<Role> allowedRoles;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;

	// ------------------------------

	public RangeVO<Integer> getPort() {
		return port;
	}

	public void setPort(RangeVO<Integer> port) {
		this.port = port;
	}

	public List<OServer> getServer() {
		return server;
	}

	public void setServer(List<OServer> server) {
		this.server = server;
	}

	public List<OService> getService() {
		return service;
	}

	public void setService(List<OService> service) {
		this.service = service;
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