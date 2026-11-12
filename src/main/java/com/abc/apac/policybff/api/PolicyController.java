package com.abc.apac.policybff.api;

import com.abc.apac.policybff.api.dto.FlagPoliciesRequest;
import com.abc.apac.policybff.api.dto.FlagPoliciesResponse;
import com.abc.apac.policybff.api.dto.PolicyPageResponse;
import com.abc.apac.policybff.api.dto.PolicyResponse;
import com.abc.apac.policybff.api.dto.PolicySummaryResponse;
import com.abc.apac.policybff.application.service.PolicyService;
import com.abc.apac.policybff.domain.entity.LineOfBusiness;
import com.abc.apac.policybff.domain.entity.PolicyStatus;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
	private final PolicyService policyService;

	public PolicyController(PolicyService policyService) {
		this.policyService = policyService;
	}

	@GetMapping
	public PolicyPageResponse listPolicies(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) PolicyStatus status,
			@RequestParam(required = false) LineOfBusiness lineOfBusiness,
			@RequestParam(required = false) String region,
			@RequestParam(required = false) LocalDate effectiveDateFrom,
			@RequestParam(required = false) LocalDate effectiveDateTo,
			@RequestParam(required = false) String search) {
		return PolicyPageResponse.from(policyService.list(
				page, size, sort, status, lineOfBusiness, region,
				effectiveDateFrom, effectiveDateTo, search));
	}

	@GetMapping("/{id}")
	public PolicyResponse getPolicy(@PathVariable UUID id) {
		return PolicyResponse.from(policyService.get(id));
	}

	@PatchMapping("/flag")
	public ResponseEntity<FlagPoliciesResponse> flagPolicies(
			@Valid @RequestBody FlagPoliciesRequest request) {
		var result = policyService.flag(request.policyIds());
		return ResponseEntity.ok(FlagPoliciesResponse.from(request.policyIds().size(), result));
	}

	@GetMapping("/summary")
	public PolicySummaryResponse getSummary(
			@RequestParam(required = false) PolicyStatus status,
			@RequestParam(required = false) LineOfBusiness lineOfBusiness,
			@RequestParam(required = false) String region,
			@RequestParam(required = false) LocalDate effectiveDateFrom,
			@RequestParam(required = false) LocalDate effectiveDateTo,
			@RequestParam(required = false) String search) {
		return PolicySummaryResponse.from(policyService.summary(
				status, lineOfBusiness, region, effectiveDateFrom, effectiveDateTo, search));
	}
}
