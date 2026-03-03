ALTER TABLE transactions
ADD COLUMN type VARCHAR(20) DEFAULT 'EXPENSE' NOT NULL,
ADD COLUMN payment_method VARCHAR(20);

COMMENT ON COLUMN transactions.type IS 'Type of transaction: INCOME or EXPENSE';
COMMENT ON COLUMN transactions.payment_method IS 'Payment method used, e.g., CREDIT, DEBIT, PIX';
