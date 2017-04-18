//overwrite
package org.devocative.ares.vo.filter.oservice;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class OSIUserFVO implements Serializable {
	private static final long serialVersionUID = -996678801L;

	private String username;
	private Boolean executor;
	private Boolean enabled;
	private List<ERemoteMode> remoteMode;
	private List<OServiceInstance> serviceInstance;
	private List<OServer> server;
	private List<OService> service;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;

	// ------------------------------

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean getExecutor() {
		return executor;
	}

	public void setExecutor(Boolean executor) {
		this.executor = executor;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<ERemoteMode> getRemoteMode() {
		return remoteMode;
	}

	public void setRemoteMode(List<ERemoteMode> remoteMode) {
		this.remoteMode = remoteMode;
	}

	public List<OServiceInstance> getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(List<OServiceInstance> serviceInstance) {
		this.serviceInstance = serviceInstance;
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