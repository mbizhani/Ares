//overwrite
package org.devocative.ares.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.ares.entity.EServerOS;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.entity.OServer;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class OServerFVO implements Serializable {
	private static final long serialVersionUID = 85077013L;

	private String name;
	private String address;
	private List<OBasicData> function;
	private RangeVO<Integer> counter;
	private List<OBasicData> environment;
	private List<OBasicData> location;
	private List<OBasicData> company;
	private String vmId;
	private List<EServerOS> serverOS;
	private List<OServer> hypervisor;
	private List<User> owner;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<OBasicData> getFunction() {
		return function;
	}

	public void setFunction(List<OBasicData> function) {
		this.function = function;
	}

	public RangeVO<Integer> getCounter() {
		return counter;
	}

	public void setCounter(RangeVO<Integer> counter) {
		this.counter = counter;
	}

	public List<OBasicData> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<OBasicData> environment) {
		this.environment = environment;
	}

	public List<OBasicData> getLocation() {
		return location;
	}

	public void setLocation(List<OBasicData> location) {
		this.location = location;
	}

	public List<OBasicData> getCompany() {
		return company;
	}

	public void setCompany(List<OBasicData> company) {
		this.company = company;
	}

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public List<EServerOS> getServerOS() {
		return serverOS;
	}

	public void setServerOS(List<EServerOS> serverOS) {
		this.serverOS = serverOS;
	}

	public List<OServer> getHypervisor() {
		return hypervisor;
	}

	public void setHypervisor(List<OServer> hypervisor) {
		this.hypervisor = hypervisor;
	}

	public List<User> getOwner() {
		return owner;
	}

	public void setOwner(List<User> owner) {
		this.owner = owner;
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