package org.devocative.ares.entity.command;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EViewMode implements Serializable {
	private static final long serialVersionUID = -5039834201695120706L;

	private static final Map<Integer, EViewMode> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final EViewMode NORMAL = new EViewMode(1, "Normal");
	public static final EViewMode HIDDEN = new EViewMode(2, "Hidden");
	public static final EViewMode LIST = new EViewMode(3, "List");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private EViewMode(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public EViewMode() {
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
		if (!(o instanceof ECommandResult)) return false;

		ECommandResult that = (ECommandResult) o;

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

	public static List<EViewMode> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
