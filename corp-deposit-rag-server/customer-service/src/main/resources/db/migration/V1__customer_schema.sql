CREATE TABLE IF NOT EXISTS customer_large_category (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_code INT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_customer_large_category_code (category_code)
);

CREATE TABLE IF NOT EXISTS customer_small_category (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    large_category_id BIGINT NOT NULL,
    category_code INT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_customer_small_category_code (category_code),
    CONSTRAINT fk_small_large_category FOREIGN KEY (large_category_id)
        REFERENCES customer_large_category (sn_id)
);

CREATE TABLE IF NOT EXISTS customer (
    sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_no VARCHAR(32) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    small_category_id BIGINT NOT NULL,
    status INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_customer_no (customer_no),
    CONSTRAINT fk_customer_small_category FOREIGN KEY (small_category_id)
        REFERENCES customer_small_category (sn_id)
);

INSERT INTO customer_large_category (sn_id, category_code, category_name, status)
VALUES (1, 3, '国有企业', 1), (2, 4, '民营企业', 1);

INSERT INTO customer_small_category (sn_id, large_category_id, category_code, category_name, status)
VALUES (1, 1, 301, '国有控股企业', 1), (2, 2, 401, '科技型民营企业', 1);

INSERT INTO customer (sn_id, customer_no, customer_name, small_category_id, status)
VALUES (1, 'CUST001', '华星科技有限公司', 2, 1),
       (2, 'CUST002', '海岳国有资本有限公司', 1, 1);

