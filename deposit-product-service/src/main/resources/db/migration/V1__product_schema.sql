CREATE TABLE IF NOT EXISTS currency_dict (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(8) NOT NULL,
    currency_name VARCHAR(64) NOT NULL,
    iso_code VARCHAR(8) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_currency_code (currency_code)
);

CREATE TABLE IF NOT EXISTS deposit_product (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(32) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    deposit_type INT NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_deposit_product_code (product_code)
);

CREATE TABLE IF NOT EXISTS deposit_product_term (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    term_code VARCHAR(16) NOT NULL,
    term_name VARCHAR(64) NOT NULL,
    term_value INT NOT NULL,
    term_unit VARCHAR(16) NOT NULL,
    term_days INT NOT NULL,
    min_open_amount BIGINT NOT NULL,
    min_retain_amount BIGINT NOT NULL,
    notice_days INT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_product_term (product_id, term_code),
    CONSTRAINT fk_term_product FOREIGN KEY (product_id) REFERENCES deposit_product (sn_id)
);

CREATE TABLE IF NOT EXISTS product_customer_scope (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    small_category_code INT NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_product_scope (product_id, small_category_code),
    CONSTRAINT fk_scope_product FOREIGN KEY (product_id) REFERENCES deposit_product (sn_id)
);

CREATE TABLE IF NOT EXISTS deposit_product_rate (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_term_id BIGINT NOT NULL,
    large_category_code INT NOT NULL,
    small_category_code INT NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    interest_rate BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    expire_date DATE NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_product_rate_version (product_id, product_term_id, large_category_code, small_category_code, currency_code, effective_date),
    CONSTRAINT fk_rate_product FOREIGN KEY (product_id) REFERENCES deposit_product (sn_id),
    CONSTRAINT fk_rate_term FOREIGN KEY (product_term_id) REFERENCES deposit_product_term (sn_id)
);

INSERT INTO currency_dict (sn_id, currency_code, currency_name, iso_code, status)
VALUES (1, '001', '人民币', 'CNY', 1);

INSERT INTO deposit_product (sn_id, product_code, product_name, deposit_type, status)
VALUES (1, 'DEMAND', '单位活期存款', 1, 1),
       (2, 'TIME', '单位定期存款', 2, 1),
       (3, 'NOTICE', '单位通知存款', 3, 1),
       (4, 'LARGE', '单位大额存单', 4, 1);

INSERT INTO deposit_product_term
    (sn_id, product_id, term_code, term_name, term_value, term_unit, term_days, min_open_amount, min_retain_amount, notice_days, status)
VALUES (11, 1, 'ON', '活期', 1, 'DAY', 1, 100, 0, NULL, 1),
       (21, 2, '3M', '三个月', 3, 'MONTH', 90, 1000000, 1000000, NULL, 1),
       (22, 2, '6M', '六个月', 6, 'MONTH', 180, 1000000, 1000000, NULL, 1),
       (23, 2, '1Y', '一年', 1, 'YEAR', 365, 1000000, 1000000, NULL, 1),
       (31, 3, '1D', '一天通知', 1, 'DAY', 1, 5000000, 5000000, 1, 1),
       (32, 3, '7D', '七天通知', 7, 'DAY', 7, 5000000, 5000000, 7, 1),
       (41, 4, '1Y', '一年', 1, 'YEAR', 365, 20000000, 20000000, NULL, 1);

INSERT INTO product_customer_scope (product_id, small_category_code, status)
VALUES (1, 301, 1), (2, 301, 1), (3, 301, 1), (4, 301, 1),
       (1, 401, 1), (2, 401, 1), (3, 401, 1), (4, 401, 1);

INSERT INTO deposit_product_rate
    (product_id, product_term_id, large_category_code, small_category_code, currency_code, interest_rate, effective_date, expire_date, status)
VALUES (1, 11, 4, 401, '001', 2500, '2026-01-01', NULL, 1),
       (2, 21, 4, 401, '001', 11000, '2026-01-01', NULL, 1),
       (2, 22, 4, 401, '001', 13000, '2026-01-01', NULL, 1),
       (2, 23, 4, 401, '001', 15000, '2026-01-01', NULL, 1),
       (3, 31, 4, 401, '001', 8000, '2026-01-01', NULL, 1),
       (3, 32, 4, 401, '001', 10000, '2026-01-01', NULL, 1),
       (4, 41, 4, 401, '001', 19000, '2026-01-01', NULL, 1),
       (1, 11, 3, 301, '001', 2600, '2026-01-01', NULL, 1),
       (2, 23, 3, 301, '001', 15500, '2026-01-01', NULL, 1),
       (3, 32, 3, 301, '001', 10500, '2026-01-01', NULL, 1),
       (4, 41, 3, 301, '001', 19500, '2026-01-01', NULL, 1);

