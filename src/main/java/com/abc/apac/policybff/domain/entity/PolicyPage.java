package com.abc.apac.policybff.domain.entity;

import java.util.List;

public record PolicyPage(
		List<Policy> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		PolicySort sort) {
}
