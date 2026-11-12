package com.abc.apac.policybff.api.dto;

import com.abc.apac.policybff.domain.entity.PolicySummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PolicySummaryResponse(
		long policyCount,
		Map<String, Long> statusCounts,
		List<PremiumTotalResponse> premiumTotalsByLineOfBusinessAndCurrency,
		long expiringSoonCount) {

	public static PolicySummaryResponse from(PolicySummary summary) {
		Map<String, Long> counts = summary.statusCounts().entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(
						entry -> entry.getKey().name(),
						Map.Entry::getValue));
		return new PolicySummaryResponse(
				summary.policyCount(),
				counts,
				summary.premiumTotalsByLineOfBusinessAndCurrency().stream()
						.map(total -> new PremiumTotalResponse(
								total.lineOfBusiness().value(),
								total.currency().name(),
								total.totalPremiumAmount()))
						.toList(),
				summary.expiringSoonCount());
	}

	public record PremiumTotalResponse(
			String lineOfBusiness,
			String currency,
			BigDecimal totalPremiumAmount) {
	}
}
