package org.devocative.ares.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EServerOS implements Serializable {
	private static final long serialVersionUID = -9198846977763809916L;

	private static final Map<Integer, EServerOS> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final EServerOS LINUX = new EServerOS(1, "Linux");
	public static final EServerOS ESX = new EServerOS(2, "ESX");
	public static final EServerOS WINDOWS = new EServerOS(3, "Windows");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private EServerOS(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public EServerOS() {
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
		if (!(o instanceof EServerOS)) return false;

		EServerOS that = (EServerOS) o;

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

	public static List<EServerOS> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
