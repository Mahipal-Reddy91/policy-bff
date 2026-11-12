package com.abc.apac.policybff.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_INTEGRATION", matches = "true")
class SqlServerMigrationIntegrationTest {
	private static final Pattern DATABASE_NAME =
			Pattern.compile("(?i)(?:databaseName|database)=([^;]+)");
	private static final String REFERENCE_DATE = "2026-11-12";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DynamicPropertySource
	static void sqlServerProperties(DynamicPropertyRegistry registry) {
		String url = requiredEnvironmentVariable("DB_TEST_URL");
		Matcher matcher = DATABASE_NAME.matcher(url);
		if (!matcher.find() || !matcher.group(1).trim().toLowerCase().endsWith("_test")) {
			throw new IllegalStateException(
					"DB_TEST_URL must explicitly name a dedicated database ending in _test.");
		}
		registry.add("spring.datasource.url", () -> url);
		registry.add("spring.datasource.username", () -> requiredEnvironmentVariable("DB_TEST_USERNAME"));
		registry.add("spring.datasource.password", () -> requiredEnvironmentVariable("DB_TEST_PASSWORD"));
		registry.add("spring.datasource.driver-class-name",
				() -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
	}

	@Test
	void migrationsAndDeterministicSeedDataMeetTheRequiredCoverage() {
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE version IN ('1', '2') AND success = 1"))
				.isEqualTo(2);
		assertThat(queryForInt("SELECT COUNT(*) FROM policies")).isEqualTo(240);
		assertThat(queryForInt("SELECT COUNT(DISTINCT status) FROM policies")).isEqualTo(4);
		assertThat(queryForInt("SELECT COUNT(DISTINCT line_of_business) FROM policies")).isEqualTo(4);
		assertThat(queryForInt("SELECT COUNT(DISTINCT region) FROM policies")).isEqualTo(8);
		assertThat(queryForInt("SELECT COUNT(DISTINCT currency) FROM policies")).isEqualTo(6);
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM policies WHERE premium_amount < 1000 OR premium_amount > 5000000"))
				.isZero();
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM policies WHERE status = 'Active' AND expiry_date <= ?",
				REFERENCE_DATE))
				.isZero();
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM policies WHERE status = 'Expired' AND expiry_date >= ?",
				REFERENCE_DATE))
				.isZero();
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM policies WHERE status = 'Pending' AND effective_date <= ?",
				REFERENCE_DATE))
				.isZero();
		assertThat(queryForInt(
				"SELECT COUNT(*) FROM policies WHERE status = 'Active' AND expiry_date BETWEEN ? AND DATEADD(DAY, 30, ?)",
				REFERENCE_DATE, REFERENCE_DATE))
				.isPositive();
	}

	private int queryForInt(String sql, Object... args) {
		Integer result = jdbcTemplate.queryForObject(sql, Integer.class, args);
		if (result == null) {
			throw new IllegalStateException("Expected an integer result for migration verification.");
		}
		return result;
	}

	private static String requiredEnvironmentVariable(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(name + " must be set for SQL Server integration tests.");
		}
		return value;
	}
}
