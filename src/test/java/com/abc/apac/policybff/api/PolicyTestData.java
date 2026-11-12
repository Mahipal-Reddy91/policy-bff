package com.abc.apac.policybff.api;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

final class PolicyTestData {
	static final UUID ACTIVE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
	static final UUID PENDING_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
	static final UUID CANCELLED_ID = UUID.fromString("00000000-0000-0000-0000-000000000103");
	static final UUID A_AND_H_ID = UUID.fromString("00000000-0000-0000-0000-000000000104");

	private PolicyTestData() {
	}

	static void clear(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("DELETE FROM policies");
	}

	static void insert(
			JdbcTemplate jdbcTemplate,
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
			boolean flaggedForReview) {
		Instant now = Instant.now();
		jdbcTemplate.update("""
				INSERT INTO policies (
					id, policy_number, policyholder_name, line_of_business, status,
					premium_amount, currency, effective_date, expiry_date, region,
					underwriter, flagged_for_review, created_at, updated_at
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				id,
				policyNumber,
				policyholderName,
				lineOfBusiness,
				status,
				premiumAmount,
				currency,
				effectiveDate,
				expiryDate,
				region,
				underwriter,
				flaggedForReview,
				now,
				now);
	}

	static void insertRequiredFixtures(JdbcTemplate jdbcTemplate) {
		clear(jdbcTemplate);
		LocalDate today = LocalDate.now();
		insert(jdbcTemplate, ACTIVE_ID, "POL-000101", "Lim Wei Ming", "Property",
				"Active", new BigDecimal("125000.00"), "SGD",
				today.minusDays(30), today.plusDays(10), "Singapore", "Aisha Tan", false);
		insert(jdbcTemplate, PENDING_ID, "POL-000102", "Tan Mei Ling", "Casualty",
				"Pending", new BigDecimal("450000.00"), "USD",
				today.minusDays(20), today.plusDays(4), "Singapore", "Lim Underwriter", true);
		insert(jdbcTemplate, CANCELLED_ID, "POL-000103", "Yuki Tanaka", "Marine",
				"Cancelled", new BigDecimal("225000.00"), "JPY",
				today.minusDays(60), today.plusDays(365), "Japan", "Kenji Sato", false);
		insert(jdbcTemplate, A_AND_H_ID, "POL-000104", "Chen Jiahao", "A&H",
				"Active", new BigDecimal("180000.00"), "HKD",
				today.minusDays(30), today.plusDays(90), "Hong Kong", "Grace Wong", false);
	}
}
