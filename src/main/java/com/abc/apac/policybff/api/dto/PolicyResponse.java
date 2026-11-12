package com.abc.apac.policybff.api.dto;

import com.abc.apac.policybff.domain.entity.Policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyResponse(
		UUID id,
		String policyNumber,
		String policyholderName,
		String lineOfBusiness,
		String status,
		BigDecimal premiumAmount,
		String currency,
		LocalDate effectiveDate,
		LocalDate expiryDate,
		String region,
		String underwriter,
		boolean flaggedForReview,
		Instant createdAt,
		Instant updatedAt) {

	public static PolicyResponse from(Policy policy) {
		return new PolicyResponse(
				policy.id(),
				policy.policyNumber(),
				policy.policyholderName(),
				policy.lineOfBusiness().value(),
				policy.status().name(),
				policy.premiumAmount(),
				policy.currency().name(),
				policy.effectiveDate(),
				policy.expiryDate(),
				policy.region(),
				policy.underwriter(),
				policy.flaggedForReview(),
				policy.createdAt(),
				policy.updatedAt());
	}
}
