package com.abc.apac.policybff.application.service;

import com.abc.apac.policybff.application.port.PolicyRepository;
import com.abc.apac.policybff.application.port.PolicyRepository.FlagResult;
import com.abc.apac.policybff.domain.entity.LineOfBusiness;
import com.abc.apac.policybff.domain.entity.Policy;
import com.abc.apac.policybff.domain.entity.PolicyFilter;
import com.abc.apac.policybff.domain.entity.PolicyPage;
import com.abc.apac.policybff.domain.entity.PolicySort;
import com.abc.apac.policybff.domain.entity.PolicySummary;
import com.abc.apac.policybff.domain.entity.PolicyStatus;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.abc.apac.policybff.application.service.PolicyExceptions.DuplicatePolicyIds;
import static com.abc.apac.policybff.application.service.PolicyExceptions.InvalidPolicyQuery;
import static com.abc.apac.policybff.application.service.PolicyExceptions.PolicyIdsNotFound;
import static com.abc.apac.policybff.application.service.PolicyExceptions.PolicyNotFound;

@Service
public class PolicyService {
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final String DEFAULT_SORT = "createdAt,desc";
	private static final Set<String> SORT_FIELDS = Set.of(
			"policyNumber", "policyholderName", "lineOfBusiness", "status",
			"premiumAmount", "currency", "effectiveDate", "expiryDate",
			"region", "underwriter", "flaggedForReview", "createdAt", "updatedAt");
	private static final Set<String> REGIONS = Set.of(
			"Singapore", "Hong Kong", "Australia", "Japan",
			"Thailand", "Indonesia", "Malaysia", "Philippines");

	private final PolicyRepository policyRepository;

	public PolicyService(PolicyRepository policyRepository) {
		this.policyRepository = policyRepository;
	}

	public PolicyPage list(
			Integer page,
			Integer size,
			String sortExpression,
			PolicyStatus status,
			LineOfBusiness lineOfBusiness,
			String region,
			LocalDate effectiveDateFrom,
			LocalDate effectiveDateTo,
			String search) {
		int resolvedPage = page == null ? DEFAULT_PAGE : page;
		int resolvedSize = size == null ? DEFAULT_SIZE : size;
		if (resolvedPage < 0) {
			throw new InvalidPolicyQuery("page", "Must be zero or greater.");
		}
		if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
			throw new InvalidPolicyQuery("size", "Must be between 1 and 100.");
		}
		validateDates(effectiveDateFrom, effectiveDateTo);
		validateRegion(region);
		PolicyFilter filter = new PolicyFilter(
				status, lineOfBusiness, normalizeRegion(region),
				effectiveDateFrom, effectiveDateTo, normalizeSearch(search));
		return policyRepository.search(filter, resolvedPage, resolvedSize, parseSort(sortExpression));
	}

	public Policy get(UUID id) {
		return policyRepository.findById(id).orElseThrow(() -> new PolicyNotFound(id));
	}

	public FlagResult flag(List<UUID> policyIds) {
		if (policyIds == null || policyIds.stream().anyMatch(id -> id == null)) {
			throw new InvalidPolicyQuery("policyIds", "Policy IDs are required and cannot be null.");
		}
		if (new HashSet<>(policyIds).size() != policyIds.size()) {
			throw new DuplicatePolicyIds();
		}
		FlagResult result = policyRepository.flagPolicies(List.copyOf(policyIds));
		if (result == null) {
			throw new IllegalStateException("The policy repository returned no bulk flag result.");
		}
		return result;
	}

	public PolicySummary summary(
			PolicyStatus status,
			LineOfBusiness lineOfBusiness,
			String region,
			LocalDate effectiveDateFrom,
			LocalDate effectiveDateTo,
			String search) {
		validateDates(effectiveDateFrom, effectiveDateTo);
		validateRegion(region);
		return policyRepository.summarize(new PolicyFilter(
				status, lineOfBusiness, normalizeRegion(region),
				effectiveDateFrom, effectiveDateTo, normalizeSearch(search)));
	}

	private PolicySort parseSort(String sortExpression) {
		String expression = sortExpression == null || sortExpression.isBlank()
				? DEFAULT_SORT
				: sortExpression.trim();
		String[] parts = expression.split(",", -1);
		if (parts.length != 2 || !SORT_FIELDS.contains(parts[0])) {
			throw new InvalidPolicyQuery("sort", "Use an allowed field and direction in field,asc|desc form.");
		}
		PolicySort.Direction direction;
		try {
			direction = PolicySort.Direction.valueOf(parts[1].toLowerCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidPolicyQuery("sort", "Sort direction must be asc or desc.");
		}
		return new PolicySort(parts[0], direction);
	}

	private void validateDates(LocalDate from, LocalDate to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new InvalidPolicyQuery("effectiveDateFrom", "Must not be after effectiveDateTo.");
		}
	}

	private void validateRegion(String region) {
		if (region != null && !region.isBlank() && !REGIONS.contains(region.trim())) {
			throw new InvalidPolicyQuery("region", "Must be one of the supported APAC regions.");
		}
	}

	private String normalizeRegion(String region) {
		return region == null || region.isBlank() ? null : region.trim();
	}

	private String normalizeSearch(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}
		return search.trim();
	}
}
