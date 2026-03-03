-- Index on user_id
CREATE INDEX idx_transactions_user_id
    ON transactions (user_id);

-- Index on category
CREATE INDEX idx_transactions_category
    ON transactions (category);

-- Index on transaction_date
CREATE INDEX idx_transactions_transaction_date
    ON transactions (transaction_date);

-- Composite index on user_id and category (for dashboard aggregations and filters)
CREATE INDEX idx_transactions_user_category
    ON transactions (user_id, category);

-- (Optional) Index on users email (used during login lookups)
CREATE INDEX idx_users_email
    ON users (email);
