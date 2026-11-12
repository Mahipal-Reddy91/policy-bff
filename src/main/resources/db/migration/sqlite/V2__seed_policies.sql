WITH RECURSIVE numbers(n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1
    FROM numbers
    WHERE n < 239
),
policy_statuses AS (
    SELECT
        n,
        CASE n % 4
            WHEN 0 THEN 'Active'
            WHEN 1 THEN 'Expired'
            WHEN 2 THEN 'Pending'
            ELSE 'Cancelled'
        END AS status
    FROM numbers
),
policy_expiries AS (
    SELECT
        n,
        status,
        CASE
            WHEN status = 'Active' AND n % 20 = 0
                THEN date('now', printf('+%d days', 1 + n / 20))
            WHEN status = 'Active'
                THEN date('now', printf('+%d days', 31 + n % 365))
            WHEN status = 'Expired'
                THEN date('now', printf('-%d days', 1 + n % 365))
            WHEN status = 'Pending'
                THEN date('now', printf('+%d days', 366 + n % 365))
            ELSE date('now', printf('%+d days', 365 - n % 730))
        END AS expiry_date
    FROM policy_statuses
),
policy_dates AS (
    SELECT
        n,
        status,
        CASE
            WHEN status = 'Pending'
                THEN date('now', printf('+%d days', 1 + n % 365))
            ELSE date(expiry_date, '-365 days')
        END AS effective_date,
        expiry_date
    FROM policy_expiries
)
INSERT OR IGNORE INTO policies (
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
    printf('00000000-0000-0000-0000-%012d', n + 1),
    printf('POL-%06d', n + 1),
    CASE n % 12
        WHEN 0 THEN 'Lim Wei Ming'
        WHEN 1 THEN 'Tan Mei Ling'
        WHEN 2 THEN 'Nguyen Minh Anh'
        WHEN 3 THEN 'Siti Nurhaliza'
        WHEN 4 THEN 'Ravi Kumar'
        WHEN 5 THEN 'Wong Ka Ho'
        WHEN 6 THEN 'Yuki Tanaka'
        WHEN 7 THEN 'Somchai Chaiyaporn'
        WHEN 8 THEN 'Maria Santos'
        WHEN 9 THEN 'Nur Aisyah'
        WHEN 10 THEN 'Chen Jiahao'
        ELSE 'Kavita Sharma'
    END,
    CASE n % 4
        WHEN 0 THEN 'Property'
        WHEN 1 THEN 'Casualty'
        WHEN 2 THEN 'A&H'
        ELSE 'Marine'
    END,
    status,
    1000 + ((n * 2087083) % 499900000) / 100.0,
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
        WHEN 0 THEN 'Singapore'
        WHEN 1 THEN 'Hong Kong'
        WHEN 2 THEN 'Australia'
        WHEN 3 THEN 'Japan'
        WHEN 4 THEN 'Thailand'
        WHEN 5 THEN 'Indonesia'
        WHEN 6 THEN 'Malaysia'
        ELSE 'Philippines'
    END,
    CASE n % 6
        WHEN 0 THEN 'Aisha Tan'
        WHEN 1 THEN 'James Wilson'
        WHEN 2 THEN 'Priya Nair'
        WHEN 3 THEN 'Kenji Sato'
        WHEN 4 THEN 'Grace Wong'
        ELSE 'Ananda Putri'
    END,
    CASE WHEN n % 17 = 0 THEN 1 ELSE 0 END,
    datetime('now', printf('-%d days', (n * 13) % 730)),
    datetime('now', printf('-%d days', (n * 13) % 730))
FROM policy_dates;
