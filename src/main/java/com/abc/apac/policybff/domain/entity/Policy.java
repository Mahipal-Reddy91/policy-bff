package com.abc.apac.policybff.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Policy(
		UUID id,
		String policyNumber,
		String policyholderName,
		LineOfBusiness lineOfBusiness,
		PolicyStatus status,
		BigDecimal premiumAmount,
		PolicyCurrency currency,
		LocalDate effectiveDate,
		LocalDate expiryDate,
		String region,
		String underwriter,
		boolean flaggedForReview,
		Instant createdAt,
		Instant updatedAt) {
}
