package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;
import java.util.List;

@XStreamAlias("service")
public class XService implements Serializable {
	private static final long serialVersionUID = 42915145952945386L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String connectionPattern;

	@XStreamAsAttribute
	private Integer adminPort;

	@XStreamAsAttribute
	private String ports;

	private List<XProperty> properties;

	private List<XValidation> validations;

	private List<XCommand> commands;

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

	public Integer getAdminPort() {
		return adminPort;
	}

	public void setAdminPort(Integer adminPort) {
		this.adminPort = adminPort;
	}

	public String getPorts() {
		return ports;
	}

	public void setPorts(String ports) {
		this.ports = ports;
	}

	public List<XProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<XProperty> properties) {
		this.properties = properties;
	}

	public List<XValidation> getValidations() {
		return validations;
	}

	public void setValidations(List<XValidation> validations) {
		this.validations = validations;
	}

	public List<XCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<XCommand> commands) {
		this.commands = commands;
	}
}
