package com.abc.apac.policybff.application.port;

import com.abc.apac.policybff.domain.entity.Policy;
import com.abc.apac.policybff.domain.entity.PolicyFilter;
import com.abc.apac.policybff.domain.entity.PolicyPage;
import com.abc.apac.policybff.domain.entity.PolicySort;
import com.abc.apac.policybff.domain.entity.PolicySummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {
	PolicyPage search(PolicyFilter filter, int page, int size, PolicySort sort);

	Optional<Policy> findById(UUID id);

	FlagResult flagPolicies(List<UUID> policyIds);

	PolicySummary summarize(PolicyFilter filter);

	record FlagResult(List<UUID> updatedIds, List<UUID> alreadyFlaggedIds) {
	}
}
