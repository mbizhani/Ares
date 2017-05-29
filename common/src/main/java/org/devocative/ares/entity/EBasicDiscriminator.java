package org.devocative.ares.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EBasicDiscriminator implements Serializable {
	private static final long serialVersionUID = 7065899631226197313L;

	private static final Map<Integer, EBasicDiscriminator> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final EBasicDiscriminator FUNCTION = new EBasicDiscriminator(1, "Function");
	public static final EBasicDiscriminator ENVIRONMENT = new EBasicDiscriminator(2, "Environment");
	public static final EBasicDiscriminator LOCATION = new EBasicDiscriminator(3, "Location");
	public static final EBasicDiscriminator COMPANY = new EBasicDiscriminator(4, "Company");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private EBasicDiscriminator(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public EBasicDiscriminator() {
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
		if (!(o instanceof EBasicDiscriminator)) return false;

		EBasicDiscriminator that = (EBasicDiscriminator) o;

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

	public static List<EBasicDiscriminator> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
