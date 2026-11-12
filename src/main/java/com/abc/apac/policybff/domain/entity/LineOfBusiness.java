package com.abc.apac.policybff.domain.entity;

public enum LineOfBusiness {
	Property,
	Casualty,
	A_AND_H("A&H"),
	Marine;

	private final String value;

	LineOfBusiness() {
		this.value = name();
	}

	LineOfBusiness(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static LineOfBusiness fromValue(String value) {
		for (LineOfBusiness lineOfBusiness : values()) {
			if (lineOfBusiness.value.equals(value)) {
				return lineOfBusiness;
			}
		}
		throw new IllegalArgumentException("Unknown line of business: " + value);
	}
}
