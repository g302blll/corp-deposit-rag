# V1.1 MVP Java Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable four-service MVP for customer-aware deposit matching, deterministic interest calculation, and idempotent intention creation.

**Architecture:** A Maven reactor contains shared contract modules and four independently runnable Spring Boot services. AI orchestration defaults to a deterministic Mock parser; all product eligibility, rate selection, interest calculation, and persistence remain Java-owned.

**Tech Stack:** Java 17, Spring Boot 3.2.9, Spring Cloud 2023.0.3, Spring Cloud Alibaba 2023.0.1.2, MyBatis-Plus 3.5.7, MySQL 8, Flyway, JUnit 5.

---

### Task 1: Build foundation and shared contracts

**Files:**
- Create: `pom.xml`, `.mvn/wrapper/maven-wrapper.properties`, `mvnw.cmd`
- Create: `common-core/pom.xml`, `common-web/pom.xml`
- Create: `common-core/src/main/java/com/ljp/corpdeposit/core/**`
- Test: `common-core/src/test/java/com/ljp/corpdeposit/core/InterestMathTest.java`

- [ ] Write tests proving rate conversion and `principal * rate * days / 365` rounds to cents with `HALF_UP`.
- [ ] Run `mvnw.cmd -pl common-core test` and confirm the test fails because `InterestMath` is absent.
- [ ] Implement `InterestMath`, enums, request/response records and error contracts without `double` or `float`.
- [ ] Re-run the module tests and commit the green result.

### Task 2: Customer query service

**Files:**
- Create: `customer-service/pom.xml`
- Create: `customer-service/src/main/java/com/ljp/corpdeposit/customer/**`
- Create: `customer-service/src/main/resources/application.yml`
- Create: `customer-service/src/main/resources/db/migration/V1__customer_schema.sql`
- Test: `customer-service/src/test/java/com/ljp/corpdeposit/customer/CustomerProfileServiceTest.java`

- [ ] Write a test that returns a customer with both category codes and rejects an unknown customer number.
- [ ] Run the focused test and confirm failure because the service is absent.
- [ ] Implement entity, mapper, service and `GET /api/v1/customers/{customerNo}`.
- [ ] Add customer/category schema plus deterministic seed data, run tests, and commit.

### Task 3: Product matching and interest service

**Files:**
- Create: `deposit-product-service/pom.xml`
- Create: `deposit-product-service/src/main/java/com/ljp/corpdeposit/product/**`
- Create: `deposit-product-service/src/main/resources/application.yml`
- Create: `deposit-product-service/src/main/resources/db/migration/V2__product_schema.sql`
- Test: `deposit-product-service/src/test/java/com/ljp/corpdeposit/product/ProductMatchServiceTest.java`

- [ ] Write tests for white-list eligibility, minimum opening amount, date-effective rate selection, deterministic interest, and exclusion when no rate exists.
- [ ] Run focused tests and confirm expected failures.
- [ ] Implement entities, mappers, query services, `InterestCalculationService`, `ProductMatchService`, and REST endpoints.
- [ ] Add four product types, terms, scopes and historical rate seed data; run tests and commit.

### Task 4: Idempotent intention service

**Files:**
- Create: `deposit-business-service/pom.xml`
- Create: `deposit-business-service/src/main/java/com/ljp/corpdeposit/business/**`
- Create: `deposit-business-service/src/main/resources/application.yml`
- Create: `deposit-business-service/src/main/resources/db/migration/V3__intention_schema.sql`
- Test: `deposit-business-service/src/test/java/com/ljp/corpdeposit/business/DepositIntentionServiceTest.java`

- [ ] Write tests proving one master with multiple details is created transactionally and duplicate idempotency keys return the original intention.
- [ ] Run focused tests and confirm expected failures.
- [ ] Implement entities, mappers, transaction service and `POST /api/v1/intentions`.
- [ ] Run tests and commit.

### Task 5: Mock-first AI orchestration

**Files:**
- Create: `ai-assistant-service/pom.xml`
- Create: `ai-assistant-service/src/main/java/com/ljp/corpdeposit/assistant/**`
- Create: `ai-assistant-service/src/main/resources/application.yml`
- Test: `ai-assistant-service/src/test/java/com/ljp/corpdeposit/assistant/MockRequirementExtractorTest.java`
- Test: `ai-assistant-service/src/test/java/com/ljp/corpdeposit/assistant/AiOrchestratorServiceTest.java`

- [ ] Write tests that extract `800万`, term preference and liquidity from Chinese text without calling a network.
- [ ] Write an orchestration test with local HTTP stubs proving customer → match → response flow.
- [ ] Run tests and confirm expected failures.
- [ ] Implement profile-scoped `RequirementExtractor`, Mock implementation, typed HTTP clients and REST controller.
- [ ] Add an opt-in Anthropic Messages client guarded by `anthropic` profile and environment-only credentials.
- [ ] Re-run tests and commit.

### Task 6: Runtime configuration, demo data, and documentation

**Files:**
- Create: `docs/sql/corporate_deposit_ai_v1.1_mysql.sql`
- Create: `README.md`
- Modify: `PROJECT_STATE.md`

- [ ] Assemble the complete MySQL script and document ports, profiles, startup order and curl examples.
- [ ] Initialize the local database using the supplied root credentials without recording the password in Git.
- [ ] Run the complete reactor tests with `mvnw.cmd test`.
- [ ] Start services, verify Nacos registrations, execute the plan and intention HTTP smoke tests, and stop the services.
- [ ] Update `PROJECT_STATE.md` with delivered scope, verification evidence and the next phase.
- [ ] Commit the verified result.

