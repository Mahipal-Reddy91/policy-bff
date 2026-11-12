package com.abc.apac.policybff.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class PolicyJpaEntity {
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "policy_number", nullable = false, length = 10, unique = true)
	private String policyNumber;

	@Column(name = "policyholder_name", nullable = false, length = 200)
	private String policyholderName;

	@Column(name = "line_of_business", nullable = false, length = 20)
	private String lineOfBusiness;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal premiumAmount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "effective_date", nullable = false)
	private LocalDate effectiveDate;

	@Column(name = "expiry_date", nullable = false)
	private LocalDate expiryDate;

	@Column(name = "region", nullable = false, length = 20)
	private String region;

	@Column(name = "underwriter", nullable = false, length = 120)
	private String underwriter;

	@Column(name = "flagged_for_review", nullable = false)
	private boolean flaggedForReview;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected PolicyJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public String getPolicyNumber() {
		return policyNumber;
	}

	public String getPolicyholderName() {
		return policyholderName;
	}

	public String getLineOfBusiness() {
		return lineOfBusiness;
	}

	public String getStatus() {
		return status;
	}

	public BigDecimal getPremiumAmount() {
		return premiumAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public LocalDate getEffectiveDate() {
		return effectiveDate;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public String getRegion() {
		return region;
	}

	public String getUnderwriter() {
		return underwriter;
	}

	public boolean isFlaggedForReview() {
		return flaggedForReview;
	}

	public void setFlaggedForReview(boolean flaggedForReview) {
		this.flaggedForReview = flaggedForReview;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
