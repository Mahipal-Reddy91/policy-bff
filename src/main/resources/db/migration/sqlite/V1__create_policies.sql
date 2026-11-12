CREATE TABLE policies (
    id VARCHAR(36) NOT NULL,
    policy_number VARCHAR(10) NOT NULL,
    policyholder_name VARCHAR(200) NOT NULL,
    line_of_business VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    premium_amount NUMERIC(19, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    region VARCHAR(20) NOT NULL,
    underwriter VARCHAR(120) NOT NULL,
    flagged_for_review INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_policies PRIMARY KEY (id),
    CONSTRAINT UQ_policies_policy_number UNIQUE (policy_number),
    CONSTRAINT CK_policies_policy_number CHECK (
        policy_number GLOB 'POL-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_policies_line_of_business CHECK (
        line_of_business IN ('Property', 'Casualty', 'A&H', 'Marine')),
    CONSTRAINT CK_policies_status CHECK (
        status IN ('Active', 'Expired', 'Pending', 'Cancelled')),
    CONSTRAINT CK_policies_premium_amount CHECK (
        premium_amount >= 1000 AND premium_amount <= 5000000),
    CONSTRAINT CK_policies_currency CHECK (
        currency IN ('USD', 'SGD', 'HKD', 'AUD', 'JPY', 'THB')),
    CONSTRAINT CK_policies_region CHECK (
        region IN (
            'Singapore', 'Hong Kong', 'Australia', 'Japan',
            'Thailand', 'Indonesia', 'Malaysia', 'Philippines'))
);

CREATE INDEX IX_policies_status ON policies (status);
CREATE INDEX IX_policies_line_of_business_currency
    ON policies (line_of_business, currency);
CREATE INDEX IX_policies_region_effective_date
    ON policies (region, effective_date);
CREATE INDEX IX_policies_expiry_date ON policies (expiry_date);
