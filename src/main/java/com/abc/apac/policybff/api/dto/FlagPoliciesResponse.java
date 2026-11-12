package com.abc.apac.policybff.api.dto;

import com.abc.apac.policybff.application.port.PolicyRepository.FlagResult;

import java.util.List;
import java.util.UUID;

public record FlagPoliciesResponse(
		int requestedCount,
		int updatedCount,
		int alreadyFlaggedCount,
		List<UUID> updatedIds,
		List<UUID> alreadyFlaggedIds) {

	public static FlagPoliciesResponse from(int requestedCount, FlagResult result) {
		return new FlagPoliciesResponse(
				requestedCount,
				result.updatedIds().size(),
				result.alreadyFlaggedIds().size(),
				result.updatedIds(),
				result.alreadyFlaggedIds());
	}
}
