package org.devocative.ares.vo.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.devocative.ares.entity.command.EViewMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("command")
public class XCommand implements Serializable {
	private static final long serialVersionUID = -3542666842098439595L;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private Integer execLimit;

	@XStreamAsAttribute
	private XCommandViewMode viewMode;

	private List<XParam> params;

	private String body;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getExecLimit() {
		return execLimit;
	}

	public void setExecLimit(Integer execLimit) {
		this.execLimit = execLimit;
	}

	public XCommandViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(XCommandViewMode viewMode) {
		this.viewMode = viewMode;
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

	public EViewMode getViewModeSafely() {
		if (getViewMode() != null) {
			switch (getViewMode()) {
				case Normal:
					return EViewMode.NORMAL;
				case Hidden:
					return EViewMode.HIDDEN;
				case List:
					return EViewMode.LIST;
			}
		}

		return EViewMode.NORMAL;
	}

	public List<XParam> getProperParams(boolean isAdmin) {
		if (isAdmin) {
			return getParams();
		} else {
			List<XParam> list = new ArrayList<>();
			for (XParam xParam : getParams()) {
				if (!xParam.isAdminOnlySafely()) {
					list.add(xParam);
				}
			}
			return list;
		}
	}
}
