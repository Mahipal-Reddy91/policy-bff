UPDATE policies
SET effective_date = strftime('%Y-%m-%d %H:%M:%f', effective_date),
    expiry_date = strftime('%Y-%m-%d %H:%M:%f', expiry_date),
    created_at = strftime('%Y-%m-%d %H:%M:%f', created_at),
    updated_at = strftime('%Y-%m-%d %H:%M:%f', updated_at);
