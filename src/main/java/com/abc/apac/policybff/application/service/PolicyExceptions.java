package com.abc.apac.policybff.application.service;

import java.util.List;
import java.util.UUID;

public final class PolicyExceptions {
	private PolicyExceptions() {
	}

	public static class PolicyNotFound extends RuntimeException {
		public PolicyNotFound(UUID id) {
			super("No policy exists with ID " + id + ".");
		}
	}

	public static class DuplicatePolicyIds extends RuntimeException {
		public DuplicatePolicyIds() {
			super("Duplicate policy IDs are not allowed.");
		}
	}

	public static class PolicyIdsNotFound extends RuntimeException {
		private final List<UUID> missingIds;

		public PolicyIdsNotFound(List<UUID> missingIds) {
			super("One or more requested policy IDs do not exist.");
			this.missingIds = List.copyOf(missingIds);
		}

		public List<UUID> missingIds() {
			return missingIds;
		}
	}

	public static class InvalidPolicyQuery extends RuntimeException {
		private final String field;

		public InvalidPolicyQuery(String field, String message) {
			super(message);
			this.field = field;
		}

		public String field() {
			return field;
		}
	}
}
