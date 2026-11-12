package com.abc.apac.policybff.domain.entity;

public record PolicySort(String field, Direction direction) {
	public enum Direction {
		asc,
		desc
	}
}
