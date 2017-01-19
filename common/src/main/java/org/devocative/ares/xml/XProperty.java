package org.devocative.ares.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

@XStreamAlias("property")
public class XProperty implements Serializable {
	private static final long serialVersionUID = -5351574537269904298L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private Boolean required;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
}
