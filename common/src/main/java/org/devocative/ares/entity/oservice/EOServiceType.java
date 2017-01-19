package org.devocative.ares.entity.oservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EOServiceType implements Serializable {
	private static final long serialVersionUID = 6855672403227289533L;

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final List<EOServiceType> ALL = new ArrayList<>();

	// ------------------------------

	public static final EOServiceType OS = new EOServiceType(1, "OS");
	public static final EOServiceType DATABASE = new EOServiceType(2, "Database");

	// ------------------------------

	private Integer id;

	// ------------------------------

	private EOServiceType(Integer id, String name) {
		this.id = id;

		ID_TO_NAME.put(id, name);
		ALL.add(this);
	}

	public EOServiceType() {
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
		if (!(o instanceof EOServiceType)) return false;

		EOServiceType that = (EOServiceType) o;

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

	public static List<EOServiceType> list() {
		return new ArrayList<>(ALL);
	}

	public static EOServiceType findByName(String name) {
		for (EOServiceType serviceType : ALL) {
			if (serviceType.getName().equals(name)) {
				return serviceType;
			}
		}

		return null;
	}
}
