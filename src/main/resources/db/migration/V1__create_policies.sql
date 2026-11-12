CREATE TABLE policies (
    id UNIQUEIDENTIFIER NOT NULL,
    policy_number NVARCHAR(10) NOT NULL,
    policyholder_name NVARCHAR(200) NOT NULL,
    line_of_business NVARCHAR(20) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    premium_amount DECIMAL(19, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    region NVARCHAR(20) NOT NULL,
    underwriter NVARCHAR(120) NOT NULL,
    flagged_for_review BIT NOT NULL
        CONSTRAINT DF_policies_flagged_for_review DEFAULT (0),
    created_at DATETIME2(7) NOT NULL
        CONSTRAINT DF_policies_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at DATETIME2(7) NOT NULL
        CONSTRAINT DF_policies_updated_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_policies PRIMARY KEY (id),
    CONSTRAINT UQ_policies_policy_number UNIQUE (policy_number),
    CONSTRAINT CK_policies_policy_number CHECK (
        policy_number LIKE N'POL-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_policies_line_of_business CHECK (
        line_of_business IN (N'Property', N'Casualty', N'A&H', N'Marine')),
    CONSTRAINT CK_policies_status CHECK (
        status IN (N'Active', N'Expired', N'Pending', N'Cancelled')),
    CONSTRAINT CK_policies_premium_amount CHECK (
        premium_amount >= 1000 AND premium_amount <= 5000000),
    CONSTRAINT CK_policies_currency CHECK (
        currency IN ('USD', 'SGD', 'HKD', 'AUD', 'JPY', 'THB')),
    CONSTRAINT CK_policies_region CHECK (
        region IN (
            N'Singapore', N'Hong Kong', N'Australia', N'Japan',
            N'Thailand', N'Indonesia', N'Malaysia', N'Philippines'))
);

CREATE INDEX IX_policies_status ON policies (status);
CREATE INDEX IX_policies_line_of_business_currency
    ON policies (line_of_business, currency);
CREATE INDEX IX_policies_region_effective_date
    ON policies (region, effective_date);
CREATE INDEX IX_policies_expiry_date ON policies (expiry_date);
