package org.devocative.ares.entity.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EServiceType implements Serializable {
	private static final long serialVersionUID = 6855672403227289533L;

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final List<EServiceType> ALL = new ArrayList<>();

	// ------------------------------

	public static final EServiceType OS = new EServiceType(1, "OS");
	public static final EServiceType DATABASE = new EServiceType(2, "Database");

	// ------------------------------

	private Integer id;

	// ------------------------------

	private EServiceType(Integer id, String name) {
		this.id = id;

		ID_TO_NAME.put(id, name);
		ALL.add(this);
	}

	public EServiceType() {
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_NAME.get(getId());
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EServiceType)) return false;

		EServiceType that = (EServiceType) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

		return true;
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

	public static List<EServiceType> list() {
		return new ArrayList<>(ALL);
	}
}
