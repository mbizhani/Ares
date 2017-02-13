package org.devocative.ares.vo.xml;

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

	public XParam setName(String name) {
		this.name = name;
		return this;
	}

	public String getType() {
		return type;
	}

	public XParam setType(String type) {
		this.type = type;
		return this;
	}

	public Boolean getRequired() {
		return required;
	}

	public XParam setRequired(Boolean required) {
		this.required = required;
		return this;
	}
}
