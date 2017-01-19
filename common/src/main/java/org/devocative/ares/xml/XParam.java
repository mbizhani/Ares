package org.devocative.ares.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

@XStreamAlias("param")
public class XParam implements Serializable {
	private static final long serialVersionUID = 2089896456604388719L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private Boolean required;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
}
