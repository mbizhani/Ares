package org.devocative.ares.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;
import java.util.List;

@XStreamAlias("command")
public class XCommand implements Serializable {
	private static final long serialVersionUID = -3542666842098439595L;

	@XStreamAsAttribute
	private String name;

	private List<XParam> params;

	private String body;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<XParam> getParams() {
		return params;
	}

	public void setParams(List<XParam> params) {
		this.params = params;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}