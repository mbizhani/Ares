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
	private XParamType type;

	@XStreamAsAttribute
	private Boolean required;

	@XStreamAsAttribute
	private String defaultValue;

	@XStreamAsAttribute
	private String stringLiterals;

	// ------------------------------

	public String getName() {
		return name;
	}

	public XParam setName(String name) {
		this.name = name;
		return this;
	}

	public XParamType getType() {
		return type != null ? type : XParamType.String;
	}

	public XParam setType(XParamType type) {
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

	public String getStringLiterals() {
		return stringLiterals;
	}

	public XParam setStringLiterals(String stringLiterals) {
		this.stringLiterals = stringLiterals;
		return this;
	}

	// ---------------

	public Object getDefaultValueObject() {
		if (getType() == XParamType.Boolean) {
			return Boolean.valueOf(getDefaultValue());
		} else {
			return getDefaultValue();
		}
	}
}
