CREATE TABLE app_users (
 id UUID PRIMARY KEY, email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL, role VARCHAR(16) NOT NULL CHECK (role IN ('CUSTOMER','ADMIN')),
 created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE accounts (
 id UUID PRIMARY KEY, owner_id UUID NOT NULL REFERENCES app_users(id),
 display_name VARCHAR(80) NOT NULL, currency VARCHAR(3) NOT NULL CHECK(currency = 'INR'),
 balance NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK(balance >= 0), created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_accounts_owner ON accounts(owner_id, created_at);
CREATE TABLE transfer_requests (
 id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_users(id),
 idempotency_key VARCHAR(128) NOT NULL, payload_hash VARCHAR(64) NOT NULL,
 status VARCHAR(16) NOT NULL CHECK(status IN ('PROCESSING','COMPLETED')),
 source_id UUID NOT NULL REFERENCES accounts(id), destination_id UUID NOT NULL REFERENCES accounts(id),
 amount NUMERIC(19,2) NOT NULL CHECK(amount > 0), created_at TIMESTAMPTZ NOT NULL,
 completed_at TIMESTAMPTZ,
 CONSTRAINT uq_transfer_key UNIQUE(user_id, idempotency_key),
 CHECK(source_id <> destination_id)
);
CREATE TABLE transactions (
 id UUID PRIMARY KEY, transfer_id UUID NOT NULL REFERENCES transfer_requests(id),
 account_id UUID NOT NULL REFERENCES accounts(id), type VARCHAR(6) NOT NULL CHECK(type IN ('DEBIT','CREDIT')),
 amount NUMERIC(19,2) NOT NULL CHECK(amount > 0), balance_after NUMERIC(19,2) NOT NULL CHECK(balance_after >= 0),
 created_at TIMESTAMPTZ NOT NULL, UNIQUE(transfer_id, type)
);
CREATE INDEX idx_transactions_account_date ON transactions(account_id, created_at DESC, id);
CREATE INDEX idx_transactions_date ON transactions(created_at);
CREATE TABLE daily_reports (
 report_date DATE PRIMARY KEY, total_debits NUMERIC(24,2) NOT NULL,
 total_credits NUMERIC(24,2) NOT NULL, transaction_count BIGINT NOT NULL,
 generated_at TIMESTAMPTZ NOT NULL
);
