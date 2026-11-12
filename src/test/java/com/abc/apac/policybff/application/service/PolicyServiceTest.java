package com.abc.apac.policybff.application.service;

import com.abc.apac.policybff.application.port.PolicyRepository;
import com.abc.apac.policybff.domain.entity.PolicyFilter;
import com.abc.apac.policybff.domain.entity.PolicyPage;
import com.abc.apac.policybff.domain.entity.PolicySort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyServiceTest {
	private PolicyRepository repository;
	private PolicyService service;

	@BeforeEach
	void setUp() {
		repository = mock(PolicyRepository.class);
		service = new PolicyService(repository);
		when(repository.search(any(), eq(0), eq(20), any()))
				.thenReturn(new PolicyPage(List.of(), 0, 20, 0, 0,
						new PolicySort("createdAt", PolicySort.Direction.desc)));
	}

	@Test
	void appliesDefaultPaginationAndSort() {
		service.list(null, null, null, null, null, null, null, null, null);

		ArgumentCaptor<PolicySort> sort = ArgumentCaptor.forClass(PolicySort.class);
		verify(repository).search(any(), eq(0), eq(20), sort.capture());
		assertThat(sort.getValue())
				.isEqualTo(new PolicySort("createdAt", PolicySort.Direction.desc));
	}

	@Test
	void rejectsInvalidPaginationAndSort() {
		assertThatThrownBy(() -> service.list(-1, 20, null, null, null, null, null, null, null))
				.isInstanceOf(PolicyExceptions.InvalidPolicyQuery.class);
		assertThatThrownBy(() -> service.list(0, 101, null, null, null, null, null, null, null))
				.isInstanceOf(PolicyExceptions.InvalidPolicyQuery.class);
		assertThatThrownBy(() -> service.list(0, 20, "id,sideways", null, null, null, null, null, null))
				.isInstanceOf(PolicyExceptions.InvalidPolicyQuery.class);
	}

	@Test
	void rejectsReversedEffectiveDateRange() {
		assertThatThrownBy(() -> service.list(
				0, 20, null, null, null, null,
				LocalDate.parse("2026-12-31"), LocalDate.parse("2026-01-01"), null))
				.isInstanceOf(PolicyExceptions.InvalidPolicyQuery.class);
	}

	@Test
	void trimsRegionAndSearchBeforeRepositoryQuery() {
		service.list(0, 20, null, null, null, " Singapore ",
				null, null, " Lim ");

		ArgumentCaptor<PolicyFilter> filter = ArgumentCaptor.forClass(PolicyFilter.class);
		verify(repository).search(filter.capture(), eq(0), eq(20), any());
		assertThat(filter.getValue().region()).isEqualTo("Singapore");
		assertThat(filter.getValue().search()).isEqualTo("Lim");
	}

	@Test
	void rejectsUnknownRegion() {
		assertThatThrownBy(() -> service.summary(
				null, null, "New Zealand", null, null, null))
				.isInstanceOf(PolicyExceptions.InvalidPolicyQuery.class);
	}
}
