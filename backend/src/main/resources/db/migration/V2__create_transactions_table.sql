CREATE TYPE category_enum AS ENUM (
    'ASSINATURAS',
    'ALIMENTACAO',
    'VESTIMENTA',
    'LAZER',
    'FILHOS'
);

CREATE TYPE frequency_enum AS ENUM (
    'MONTHLY',
    'ANNUAL'
);

CREATE TABLE transactions (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    description      VARCHAR(500)   NOT NULL,
    amount           NUMERIC(12, 2) NOT NULL,
    category         VARCHAR(50)    NOT NULL,
    transaction_date DATE           NOT NULL,
    is_recurrent     BOOLEAN        NOT NULL DEFAULT FALSE,
    frequency        VARCHAR(20),
    user_id          UUID           NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_transactions           PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user      FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_amount_positive       CHECK (amount > 0),
    CONSTRAINT chk_frequency_recurrence  CHECK (
        (is_recurrent = FALSE AND frequency IS NULL) OR
        (is_recurrent = TRUE  AND frequency IS NOT NULL)
    )
);

COMMENT ON TABLE  transactions                IS 'Financial transactions linked to a user';
COMMENT ON COLUMN transactions.amount         IS 'Monetary value — always positive, stored with 2 decimal places';
COMMENT ON COLUMN transactions.category       IS 'Expense category stored as VARCHAR matching the Java enum name';
COMMENT ON COLUMN transactions.is_recurrent   IS 'Whether this transaction repeats periodically';
COMMENT ON COLUMN transactions.frequency      IS 'Recurrence frequency — NULL when is_recurrent is false';
