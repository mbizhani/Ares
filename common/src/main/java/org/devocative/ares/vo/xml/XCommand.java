package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;
import java.util.List;

@XStreamAlias("command")
public class XCommand implements Serializable {
	private static final long serialVersionUID = -3542666842098439595L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private Boolean listView;

	private List<XParam> params;

	private String body;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getListView() {
		return listView;
	}

	public void setListView(Boolean listView) {
		this.listView = listView;
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

	// ---------------

	public boolean checkHasGuest() {
		for (XParam xParam : getParams()) {
			if (xParam.getType() == XParamType.Guest) {
				return true;
			}
		}
		return false;
	}

	public Boolean getListViewSafely() {
		return getListView() != null ? getListView() : false;
	}

}
