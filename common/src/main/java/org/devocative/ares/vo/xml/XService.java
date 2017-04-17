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

	private List<XProperty> properties;

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

	public List<XProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<XProperty> properties) {
		this.properties = properties;
	}

	public List<XCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<XCommand> commands) {
		this.commands = commands;
	}
}
