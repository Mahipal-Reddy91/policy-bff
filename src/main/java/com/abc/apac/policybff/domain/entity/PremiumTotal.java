package com.abc.apac.policybff.domain.entity;

import java.math.BigDecimal;

public record PremiumTotal(
		LineOfBusiness lineOfBusiness,
		PolicyCurrency currency,
		BigDecimal totalPremiumAmount) {
}
