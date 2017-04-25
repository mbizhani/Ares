//overwrite
package org.devocative.ares.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class TerminalConnectionFVO implements Serializable {
	private static final long serialVersionUID = -1092326563L;

	private Boolean active;
	private RangeVO<Date> disconnection;
	private List<OSIUser> target;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;

	// ------------------------------

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public RangeVO<Date> getDisconnection() {
		return disconnection;
	}

	public void setDisconnection(RangeVO<Date> disconnection) {
		this.disconnection = disconnection;
	}

	public List<OSIUser> getTarget() {
		return target;
	}

	public void setTarget(List<OSIUser> target) {
		this.target = target;
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