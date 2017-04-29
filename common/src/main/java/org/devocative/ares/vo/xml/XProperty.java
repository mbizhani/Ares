package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

@XStreamAlias("property")
public class XProperty implements Serializable {
	private static final long serialVersionUID = -5351574537269904298L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String value;

	@XStreamAsAttribute
	private Boolean required;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getRequired() {
		return required != null ? required : false;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
}
