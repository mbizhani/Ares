package org.devocative.ares.entity.command;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ECommandResult implements Serializable {
	private static final long serialVersionUID = -4112941408007307058L;

	private static final Map<Integer, ECommandResult> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ECommandResult UNKNOWN = new ECommandResult(0, "Unknown");
	public static final ECommandResult RUNNING = new ECommandResult(1, "Running");
	public static final ECommandResult SUCCESSFUL = new ECommandResult(2, "Successful");
	public static final ECommandResult ERROR = new ECommandResult(3, "Error");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ECommandResult(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ECommandResult() {
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

	public static List<ECommandResult> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

}
