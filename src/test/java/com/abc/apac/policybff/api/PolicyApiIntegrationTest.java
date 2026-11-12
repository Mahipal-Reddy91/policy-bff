package com.abc.apac.policybff.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.abc.apac.policybff.api.PolicyTestData.ACTIVE_ID;
import static com.abc.apac.policybff.api.PolicyTestData.A_AND_H_ID;
import static com.abc.apac.policybff.api.PolicyTestData.PENDING_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PolicyApiIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		PolicyTestData.insertRequiredFixtures(jdbcTemplate);
	}

	@Test
	void listPoliciesAppliesFiltersAndPagination() throws Exception {
		mockMvc.perform(get("/api/v1/policies")
						.param("page", "0")
						.param("size", "1")
						.param("sort", "premiumAmount,desc")
						.param("region", "Singapore")
						.param("search", "wei"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].policyNumber").value("POL-000101"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.sort.field").value("premiumAmount"))
				.andExpect(jsonPath("$.sort.direction").value("desc"));
	}

	@Test
	void listPoliciesRejectsInvalidPageSizeAndDateRange() throws Exception {
		mockMvc.perform(get("/api/v1/policies").param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		mockMvc.perform(get("/api/v1/policies")
						.param("effectiveDateFrom", "2026-12-31")
						.param("effectiveDateTo", "2026-01-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getsPolicyAndReturnsProblemForMissingPolicy() throws Exception {
		mockMvc.perform(get("/api/v1/policies/{id}", ACTIVE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyNumber").value("POL-000101"));
		mockMvc.perform(get("/api/v1/policies/{id}", "00000000-0000-0000-0000-000000000999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
	}

	@Test
	void bulkFlagIsAtomicForMissingAndDuplicateIds() throws Exception {
		String missingBody = """
				{"policyIds":["%s","00000000-0000-0000-0000-000000000999"]}
				""".formatted(ACTIVE_ID);
		mockMvc.perform(patch("/api/v1/policies/flag")
						.contentType(MediaType.APPLICATION_JSON)
						.content(missingBody))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("POLICY_IDS_NOT_FOUND"));
		org.assertj.core.api.Assertions.assertThat(isFlagged(ACTIVE_ID)).isFalse();

		String duplicateBody = """
				{"policyIds":["%s","%s"]}
				""".formatted(ACTIVE_ID, ACTIVE_ID);
		mockMvc.perform(patch("/api/v1/policies/flag")
						.contentType(MediaType.APPLICATION_JSON)
						.content(duplicateBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("DUPLICATE_POLICY_IDS"));
		org.assertj.core.api.Assertions.assertThat(isFlagged(ACTIVE_ID)).isFalse();
	}

	@Test
	void bulkFlagReportsChangesAndIsIdempotent() throws Exception {
		String body = """
				{"policyIds":["%s","%s"]}
				""".formatted(ACTIVE_ID, PENDING_ID);
		mockMvc.perform(patch("/api/v1/policies/flag")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requestedCount").value(2))
				.andExpect(jsonPath("$.updatedCount").value(1))
				.andExpect(jsonPath("$.alreadyFlaggedCount").value(1));
		mockMvc.perform(patch("/api/v1/policies/flag")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedCount").value(0))
				.andExpect(jsonPath("$.alreadyFlaggedCount").value(2));
	}

	@Test
	void summaryGroupsPremiumByCurrencyAndCountsAllStatusesExpiringSoon() throws Exception {
		mockMvc.perform(get("/api/v1/policies/summary").param("region", "Singapore"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyCount").value(2))
				.andExpect(jsonPath("$.statusCounts.Active").value(1))
				.andExpect(jsonPath("$.statusCounts.Pending").value(1))
				.andExpect(jsonPath("$.statusCounts.Expired").value(0))
				.andExpect(jsonPath("$.premiumTotalsByLineOfBusinessAndCurrency", hasSize(2)))
				.andExpect(jsonPath("$.expiringSoonCount").value(2));
	}

	@Test
	void acceptsEmptyBulkRequestAsNoOp() throws Exception {
		mockMvc.perform(patch("/api/v1/policies/flag")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"policyIds\":[]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requestedCount").value(0))
				.andExpect(jsonPath("$.updatedCount").value(0));
	}

	@Test
	void acceptsContractLineOfBusinessValueForListAndSummary() throws Exception {
		mockMvc.perform(get("/api/v1/policies")
						.param("lineOfBusiness", "A&H"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].id").value(A_AND_H_ID.toString()))
				.andExpect(jsonPath("$.content[0].lineOfBusiness").value("A&H"));

		mockMvc.perform(get("/api/v1/policies/summary")
						.param("lineOfBusiness", "A&H"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyCount").value(1));
	}

	@Test
	void servesOpenApiContractAtConfiguredSwaggerUrl() throws Exception {
		mockMvc.perform(get("/openapi/policy-api.yaml"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("openapi: 3.0.3")));
	}

	private boolean isFlagged(java.util.UUID id) {
		return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
				"SELECT flagged_for_review FROM policies WHERE id = ?",
				Boolean.class,
				id));
	}
}
