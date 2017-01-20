package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.List;

@XStreamAlias("operation")
public class XOperation implements Serializable {
	private static final long serialVersionUID = -4246699790739022744L;

	private List<XService> services;

	// ------------------------------

	public List<XService> getServices() {
		return services;
	}

	public void setServices(List<XService> services) {
		this.services = services;
	}
}
