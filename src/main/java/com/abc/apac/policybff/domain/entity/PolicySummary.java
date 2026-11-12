package com.abc.apac.policybff.domain.entity;

import java.util.List;
import java.util.Map;

public record PolicySummary(
		long policyCount,
		Map<PolicyStatus, Long> statusCounts,
		List<PremiumTotal> premiumTotalsByLineOfBusinessAndCurrency,
		long expiringSoonCount) {
}
