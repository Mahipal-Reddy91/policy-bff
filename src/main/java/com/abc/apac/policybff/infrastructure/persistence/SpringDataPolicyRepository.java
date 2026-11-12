package com.abc.apac.policybff.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpringDataPolicyRepository
		extends JpaRepository<PolicyJpaEntity, UUID>, JpaSpecificationExecutor<PolicyJpaEntity> {
}
