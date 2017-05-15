package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

@XStreamAlias("param")
public class XParam implements Serializable {
	private static final long serialVersionUID = 2089896456604388719L;

	public static final String GUEST_TYPE = "Guest";
	public static final String SERVER_TYPE = "Server";
	public static final String BOOLEAN_TYPE = "boolean";

	// ------------------------------

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private Boolean required;

	@XStreamAsAttribute
	private String defaultValue;

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
		return required != null ? required : false;
	}

	public XParam setRequired(Boolean required) {
		this.required = required;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public XParam setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	// ---------------

	public Object getDefaultValueObject() {
		if (BOOLEAN_TYPE.equals(getType())) {
			return Boolean.valueOf(getDefaultValue());
		} else {
			return getDefaultValue();
		}
	}
}
