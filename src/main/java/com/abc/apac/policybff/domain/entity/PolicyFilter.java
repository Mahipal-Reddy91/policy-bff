package com.abc.apac.policybff.domain.entity;

import java.time.LocalDate;

public record PolicyFilter(
		PolicyStatus status,
		LineOfBusiness lineOfBusiness,
		String region,
		LocalDate effectiveDateFrom,
		LocalDate effectiveDateTo,
		String search) {
}
