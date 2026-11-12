package com.abc.apac.policybff.api.dto;

import com.abc.apac.policybff.domain.entity.PolicyPage;

import java.util.List;

public record PolicyPageResponse(
		List<PolicyResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		SortResponse sort) {

	public static PolicyPageResponse from(PolicyPage page) {
		return new PolicyPageResponse(
				page.content().stream().map(PolicyResponse::from).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				new SortResponse(page.sort().field(), page.sort().direction().name()));
	}

	public record SortResponse(String field, String direction) {
	}
}
