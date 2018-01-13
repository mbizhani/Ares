//overwrite
package org.devocative.ares.vo.filter.command;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.command.EViewMode;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class CommandFVO implements Serializable {
	private static final long serialVersionUID = -2029421249L;

	private String name;
	private Boolean enabled;
	private RangeVO<Integer> execLimit;
	private List<EViewMode> viewMode;
	private Boolean confirm;
	private List<OService> service;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public RangeVO<Integer> getExecLimit() {
		return execLimit;
	}

	public void setExecLimit(RangeVO<Integer> execLimit) {
		this.execLimit = execLimit;
	}

	public List<EViewMode> getViewMode() {
		return viewMode;
	}

	public void setViewMode(List<EViewMode> viewMode) {
		this.viewMode = viewMode;
	}

	public Boolean getConfirm() {
		return confirm;
	}

	public void setConfirm(Boolean confirm) {
		this.confirm = confirm;
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