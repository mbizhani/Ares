package org.devocative.ares.entity.oservice;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ERemoteMode {
	SSH(1, "SSH"),
	JDBC(2, "JDBC");
	//HTTP(3, "HTTP"); TODO

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	ERemoteMode(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	// ------------------------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ERemoteMode> list() {
		return Arrays.asList(values());
	}

	public static ERemoteMode findByName(String name) {
		for (ERemoteMode remoteMode : values()) {
			if (remoteMode.getName().equals(name)) {
				return remoteMode;
			}
		}
		return null;
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ERemoteMode, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ERemoteMode attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public ERemoteMode convertToEntityAttribute(Integer dbData) {
			for (ERemoteMode literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}

			return null;
		}
	}

}
