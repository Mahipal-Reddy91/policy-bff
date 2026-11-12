DECLARE @referenceDate DATE = CONVERT(DATE, '2026-11-12');
DECLARE @seedTimestamp DATETIME2(7) = CONVERT(DATETIME2(7), '2026-11-12T00:00:00');

;WITH numbers AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1
    FROM numbers
    WHERE n < 239
),
policy_statuses AS (
    SELECT
        n,
        CASE n % 4
            WHEN 0 THEN N'Active'
            WHEN 1 THEN N'Expired'
            WHEN 2 THEN N'Pending'
            ELSE N'Cancelled'
        END AS status
    FROM numbers
),
policy_expiries AS (
    SELECT
        n,
        status,
        CASE
            WHEN status = N'Active' AND n % 20 = 0
                THEN DATEADD(DAY, 1 + n / 20, @referenceDate)
            WHEN status = N'Active'
                THEN DATEADD(DAY, 31 + n % 365, @referenceDate)
            WHEN status = N'Expired'
                THEN DATEADD(DAY, -(1 + n % 365), @referenceDate)
            WHEN status = N'Pending'
                THEN DATEADD(DAY, 366 + n % 365, @referenceDate)
            ELSE DATEADD(DAY, 365 - n % 730, @referenceDate)
        END AS expiry_date
    FROM policy_statuses
),
policy_dates AS (
    SELECT
        n,
        status,
        CASE
            WHEN status = N'Pending'
                THEN DATEADD(DAY, 1 + n % 365, @referenceDate)
            ELSE DATEADD(DAY, -365, expiry_date)
        END AS effective_date,
        expiry_date
    FROM policy_expiries
)
INSERT INTO policies (
    id,
    policy_number,
    policyholder_name,
    line_of_business,
    status,
    premium_amount,
    currency,
    effective_date,
    expiry_date,
    region,
    underwriter,
    flagged_for_review,
    created_at,
    updated_at
)
SELECT
    CONVERT(UNIQUEIDENTIFIER,
        CONCAT('00000000-0000-0000-0000-', RIGHT(
            CONCAT('000000000000', CONVERT(VARCHAR(12), n + 1)), 12))),
    CONCAT(N'POL-', RIGHT(CONCAT('000000', CONVERT(VARCHAR(6), n + 1)), 6)),
    CASE n % 12
        WHEN 0 THEN N'Lim Wei Ming'
        WHEN 1 THEN N'Tan Mei Ling'
        WHEN 2 THEN N'Nguyen Minh Anh'
        WHEN 3 THEN N'Siti Nurhaliza'
        WHEN 4 THEN N'Ravi Kumar'
        WHEN 5 THEN N'Wong Ka Ho'
        WHEN 6 THEN N'Yuki Tanaka'
        WHEN 7 THEN N'Somchai Chaiyaporn'
        WHEN 8 THEN N'Maria Santos'
        WHEN 9 THEN N'Nur Aisyah'
        WHEN 10 THEN N'Chen Jiahao'
        ELSE N'Kavita Sharma'
    END,
    CASE n % 4
        WHEN 0 THEN N'Property'
        WHEN 1 THEN N'Casualty'
        WHEN 2 THEN N'A&H'
        ELSE N'Marine'
    END,
    status,
    CONVERT(DECIMAL(19, 2),
        1000 + ((n * 2087083) % 499900000) / 100.0),
    CASE n % 6
        WHEN 0 THEN 'USD'
        WHEN 1 THEN 'SGD'
        WHEN 2 THEN 'HKD'
        WHEN 3 THEN 'AUD'
        WHEN 4 THEN 'JPY'
        ELSE 'THB'
    END,
    effective_date,
    expiry_date,
    CASE n % 8
        WHEN 0 THEN N'Singapore'
        WHEN 1 THEN N'Hong Kong'
        WHEN 2 THEN N'Australia'
        WHEN 3 THEN N'Japan'
        WHEN 4 THEN N'Thailand'
        WHEN 5 THEN N'Indonesia'
        WHEN 6 THEN N'Malaysia'
        ELSE N'Philippines'
    END,
    CASE n % 6
        WHEN 0 THEN N'Aisha Tan'
        WHEN 1 THEN N'James Wilson'
        WHEN 2 THEN N'Priya Nair'
        WHEN 3 THEN N'Kenji Sato'
        WHEN 4 THEN N'Grace Wong'
        ELSE N'Ananda Putri'
    END,
    CONVERT(BIT, CASE WHEN n % 17 = 0 THEN 1 ELSE 0 END),
    DATEADD(DAY, -((n * 13) % 730), @seedTimestamp),
    DATEADD(DAY, -((n * 13) % 730), @seedTimestamp)
FROM policy_dates
OPTION (MAXRECURSION 240);
