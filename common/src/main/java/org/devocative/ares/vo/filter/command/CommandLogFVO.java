//overwrite
package org.devocative.ares.vo.filter.command;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.ECommandResult;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class CommandLogFVO implements Serializable {
	private static final long serialVersionUID = 1961844763L;

	private String params;
	private List<ECommandResult> result;
	private RangeVO<Long> duration;
	private String error;
	private List<Command> command;
	private List<OServiceInstance> serviceInstance;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;

	// ------------------------------

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public List<ECommandResult> getResult() {
		return result;
	}

	public void setResult(List<ECommandResult> result) {
		this.result = result;
	}

	public RangeVO<Long> getDuration() {
		return duration;
	}

	public void setDuration(RangeVO<Long> duration) {
		this.duration = duration;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
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

}