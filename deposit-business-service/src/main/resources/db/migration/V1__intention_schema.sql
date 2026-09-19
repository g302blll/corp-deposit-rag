CREATE TABLE IF NOT EXISTS deposit_intention (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    intention_no VARCHAR(40) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    customer_no VARCHAR(32) NOT NULL,
    requirement_text VARCHAR(1000) NULL,
    total_amount BIGINT NOT NULL,
    status INT NOT NULL,
    source_channel INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_intention_no (intention_no),
    UNIQUE KEY uk_intention_idempotency (idempotency_key)
);

CREATE TABLE IF NOT EXISTS deposit_intention_detail (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    intention_id BIGINT NOT NULL,
    detail_no VARCHAR(40) NOT NULL,
    product_id BIGINT NOT NULL,
    product_term_id BIGINT NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    amount BIGINT NOT NULL,
    interest_rate BIGINT NOT NULL,
    expected_interest BIGINT NOT NULL,
    status INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_intention_detail_no (detail_no),
    KEY idx_intention_detail_master (intention_id),
    CONSTRAINT fk_intention_detail_master FOREIGN KEY (intention_id) REFERENCES deposit_intention (sn_id)
);

