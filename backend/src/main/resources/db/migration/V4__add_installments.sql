ALTER TABLE transactions
ADD COLUMN installments_count INTEGER,
ADD COLUMN installment_number INTEGER;

COMMENT ON COLUMN transactions.installments_count IS 'Total number of installments, if any';
COMMENT ON COLUMN transactions.installment_number IS 'Current installment number, if any';
