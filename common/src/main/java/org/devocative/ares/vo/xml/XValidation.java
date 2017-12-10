package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

@XStreamAlias("validation")
public class XValidation implements Serializable {
	private static final long serialVersionUID = 299152638147063740L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String regex;

	// ------------------------------

	public String getName() {
		return name;
	}

	public XValidation setName(String name) {
		this.name = name;
		return this;
	}

	public String getRegex() {
		return regex;
	}

	public XValidation setRegex(String regex) {
		this.regex = regex;
		return this;
	}
}
