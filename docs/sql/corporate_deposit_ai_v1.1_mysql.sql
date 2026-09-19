-- V1.1 MVP 统一初始化入口。
-- 在项目根目录使用 MySQL CLI 执行本文件；Flyway 迁移仍是各服务的权威建表来源。
CREATE DATABASE IF NOT EXISTS corporate_deposit_ai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE corporate_deposit_ai;

SOURCE customer-service/src/main/resources/db/migration/V1__customer_schema.sql;
SOURCE deposit-product-service/src/main/resources/db/migration/V1__product_schema.sql;
SOURCE deposit-business-service/src/main/resources/db/migration/V1__intention_schema.sql;

