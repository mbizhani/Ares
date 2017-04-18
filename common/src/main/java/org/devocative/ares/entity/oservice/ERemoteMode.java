package org.devocative.ares.entity.oservice;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ERemoteMode implements Serializable {
	private static final long serialVersionUID = -771802110362958021L;

	private static final Map<Integer, ERemoteMode> ID_TO_LIT = new LinkedHashMap<>();
	// ------------------------------

	public static final ERemoteMode SSH = new ERemoteMode(1, "SSH");
	public static final ERemoteMode JDBC = new ERemoteMode(2, "JDBC");
	public static final ERemoteMode HTTP = new ERemoteMode(3, "HTTP");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ERemoteMode(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ERemoteMode() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_LIT.get(getId()).name;
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ERemoteMode)) return false;

		ERemoteMode that = (ERemoteMode) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ERemoteMode> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
