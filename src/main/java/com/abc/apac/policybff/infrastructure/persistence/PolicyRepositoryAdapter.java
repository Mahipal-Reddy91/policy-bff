package com.abc.apac.policybff.infrastructure.persistence;

import com.abc.apac.policybff.application.port.PolicyRepository;
import com.abc.apac.policybff.application.service.PolicyExceptions.PolicyIdsNotFound;
import com.abc.apac.policybff.domain.entity.LineOfBusiness;
import com.abc.apac.policybff.domain.entity.Policy;
import com.abc.apac.policybff.domain.entity.PolicyCurrency;
import com.abc.apac.policybff.domain.entity.PolicyFilter;
import com.abc.apac.policybff.domain.entity.PolicyPage;
import com.abc.apac.policybff.domain.entity.PolicySort;
import com.abc.apac.policybff.domain.entity.PolicyStatus;
import com.abc.apac.policybff.domain.entity.PolicySummary;
import com.abc.apac.policybff.domain.entity.PremiumTotal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class PolicyRepositoryAdapter implements PolicyRepository {
	private static final Map<String, String> SORT_PROPERTIES = Map.ofEntries(
			Map.entry("policyNumber", "policyNumber"),
			Map.entry("policyholderName", "policyholderName"),
			Map.entry("lineOfBusiness", "lineOfBusiness"),
			Map.entry("status", "status"),
			Map.entry("premiumAmount", "premiumAmount"),
			Map.entry("currency", "currency"),
			Map.entry("effectiveDate", "effectiveDate"),
			Map.entry("expiryDate", "expiryDate"),
			Map.entry("region", "region"),
			Map.entry("underwriter", "underwriter"),
			Map.entry("flaggedForReview", "flaggedForReview"),
			Map.entry("createdAt", "createdAt"),
			Map.entry("updatedAt", "updatedAt"));

	private final SpringDataPolicyRepository repository;
	private final EntityManager entityManager;

	public PolicyRepositoryAdapter(SpringDataPolicyRepository repository, EntityManager entityManager) {
		this.repository = repository;
		this.entityManager = entityManager;
	}

	@Override
	@Transactional(readOnly = true)
	public PolicyPage search(PolicyFilter filter, int page, int size, PolicySort policySort) {
		String property = SORT_PROPERTIES.get(policySort.field());
		Sort.Direction direction = policySort.direction() == PolicySort.Direction.asc
				? Sort.Direction.ASC
				: Sort.Direction.DESC;
		Sort sort = Sort.by(direction, property).and(Sort.by(Sort.Direction.ASC, "id"));
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<PolicyJpaEntity> result = repository.findAll(specification(filter), pageable);
		return new PolicyPage(
				result.getContent().stream().map(PolicyRepositoryAdapter::toDomain).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				policySort);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Policy> findById(UUID id) {
		return repository.findById(id).map(PolicyRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional
	public FlagResult flagPolicies(List<UUID> policyIds) {
		if (policyIds.isEmpty()) {
			return new FlagResult(List.of(), List.of());
		}
		List<PolicyJpaEntity> entities = repository.findAllById(policyIds);
		Set<UUID> foundIds = entities.stream()
				.map(PolicyJpaEntity::getId)
				.collect(Collectors.toSet());
		List<UUID> missingIds = policyIds.stream().filter(id -> !foundIds.contains(id)).toList();
		if (!missingIds.isEmpty()) {
			throw new PolicyIdsNotFound(missingIds);
		}
		List<UUID> updatedIds = new ArrayList<>();
		List<UUID> alreadyFlaggedIds = new ArrayList<>();
		Instant now = Instant.now();
		for (PolicyJpaEntity entity : entities) {
			if (entity.isFlaggedForReview()) {
				alreadyFlaggedIds.add(entity.getId());
			}
			else {
				entity.setFlaggedForReview(true);
				entity.setUpdatedAt(now);
				updatedIds.add(entity.getId());
			}
		}
		repository.saveAll(entities);
		return new FlagResult(List.copyOf(updatedIds), List.copyOf(alreadyFlaggedIds));
	}

	@Override
	@Transactional(readOnly = true)
	public PolicySummary summarize(PolicyFilter filter) {
		Map<PolicyStatus, Long> statusCounts = new EnumMap<>(PolicyStatus.class);
		for (PolicyStatus status : PolicyStatus.values()) {
			statusCounts.put(status, 0L);
		}

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> statusQuery = builder.createTupleQuery();
		Root<PolicyJpaEntity> statusRoot = statusQuery.from(PolicyJpaEntity.class);
		statusQuery.multiselect(statusRoot.get("status"), builder.count(statusRoot));
		statusQuery.where(predicates(statusRoot, builder, filter));
		statusQuery.groupBy(statusRoot.get("status"));
		for (Tuple tuple : entityManager.createQuery(statusQuery).getResultList()) {
			statusCounts.put(PolicyStatus.valueOf(tuple.get(0, String.class)), tuple.get(1, Long.class));
		}

		CriteriaQuery<Tuple> premiumQuery = builder.createTupleQuery();
		Root<PolicyJpaEntity> premiumRoot = premiumQuery.from(PolicyJpaEntity.class);
		premiumQuery.multiselect(
				premiumRoot.get("lineOfBusiness"),
				premiumRoot.get("currency"),
				builder.sum(premiumRoot.<BigDecimal>get("premiumAmount")));
		premiumQuery.where(predicates(premiumRoot, builder, filter));
		premiumQuery.groupBy(premiumRoot.get("lineOfBusiness"), premiumRoot.get("currency"));
		List<PremiumTotal> premiumTotals = entityManager.createQuery(premiumQuery).getResultList().stream()
				.map(tuple -> new PremiumTotal(
						LineOfBusiness.fromValue(tuple.get(0, String.class)),
						PolicyCurrency.valueOf(tuple.get(1, String.class)),
						tuple.get(2, BigDecimal.class)))
				.toList();

		LocalDate today = LocalDate.now();
		CriteriaQuery<Long> expiringQuery = builder.createQuery(Long.class);
		Root<PolicyJpaEntity> expiringRoot = expiringQuery.from(PolicyJpaEntity.class);
		List<Predicate> expiringPredicates = new ArrayList<>(
				List.of(predicates(expiringRoot, builder, filter)));
		expiringPredicates.add(builder.between(
				expiringRoot.get("expiryDate"), today, today.plusDays(30)));
		expiringQuery.select(builder.count(expiringRoot));
		expiringQuery.where(expiringPredicates.toArray(Predicate[]::new));
		long expiringSoon = entityManager.createQuery(expiringQuery).getSingleResult();

		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<PolicyJpaEntity> countRoot = countQuery.from(PolicyJpaEntity.class);
		countQuery.select(builder.count(countRoot));
		countQuery.where(predicates(countRoot, builder, filter));
		long policyCount = entityManager.createQuery(countQuery).getSingleResult();

		return new PolicySummary(policyCount, Map.copyOf(statusCounts), premiumTotals, expiringSoon);
	}

	private Specification<PolicyJpaEntity> specification(PolicyFilter filter) {
		return (root, query, builder) -> predicates(root, builder, filter);
	}

	private Predicate predicates(
			Root<PolicyJpaEntity> root,
			CriteriaBuilder builder,
			PolicyFilter filter) {
		List<Predicate> predicates = new ArrayList<>();
		if (filter.status() != null) {
			predicates.add(builder.equal(root.get("status"), filter.status().name()));
		}
		if (filter.lineOfBusiness() != null) {
			predicates.add(builder.equal(root.get("lineOfBusiness"), filter.lineOfBusiness().value()));
		}
		if (filter.region() != null) {
			predicates.add(builder.equal(root.get("region"), filter.region()));
		}
		if (filter.effectiveDateFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					root.get("effectiveDate"), filter.effectiveDateFrom()));
		}
		if (filter.effectiveDateTo() != null) {
			predicates.add(builder.lessThanOrEqualTo(
					root.get("effectiveDate"), filter.effectiveDateTo()));
		}
		if (filter.search() != null && !filter.search().isBlank()) {
			String pattern = "%" + escapeLike(filter.search().toLowerCase()) + "%";
			predicates.add(builder.or(
					builder.like(builder.lower(root.get("policyNumber")), pattern, '\\'),
					builder.like(builder.lower(root.get("policyholderName")), pattern, '\\'),
					builder.like(builder.lower(root.get("underwriter")), pattern, '\\')));
		}
		return builder.and(predicates.toArray(Predicate[]::new));
	}

	private String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static Policy toDomain(PolicyJpaEntity entity) {
		return new Policy(
				entity.getId(),
				entity.getPolicyNumber(),
				entity.getPolicyholderName(),
				LineOfBusiness.fromValue(entity.getLineOfBusiness()),
				PolicyStatus.valueOf(entity.getStatus()),
				entity.getPremiumAmount(),
				PolicyCurrency.valueOf(entity.getCurrency()),
				entity.getEffectiveDate(),
				entity.getExpiryDate(),
				entity.getRegion(),
				entity.getUnderwriter(),
				entity.isFlaggedForReview(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}
