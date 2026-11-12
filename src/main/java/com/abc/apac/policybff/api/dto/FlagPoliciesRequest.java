package com.abc.apac.policybff.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FlagPoliciesRequest(@NotNull List<@NotNull UUID> policyIds) {
}
