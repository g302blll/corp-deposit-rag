# V1.3 查询接口与前端真实数据 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为客户、产品、办理意向补齐只读查询接口，并让现有 Vue 页面完全改用真实服务数据。

**Architecture:** 各后端服务只查询自身拥有的表，分别暴露客户、产品和意向 API；前端 API 适配层把后端 BIGINT ID 转成字符串，页面侧通过客户和产品字典补全意向展示名称。现有 AI 推荐、收益试算、创建意向及 Mock LLM 链路保持不变。

**Tech Stack:** JDK 17、Spring Boot 3、MyBatis、MySQL 8、JUnit 5、AssertJ、MockMvc、Vue 3、TypeScript、Vite、Axios、Vitest、Element Plus

---

## 文件结构

### 后端新增文件

- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductRow.java`：产品基础查询行。
- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductSummary.java`：产品列表响应。
- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductTermView.java`：产品期限响应。
- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductDetail.java`：产品详情响应。
- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductQueryRepository.java`：产品只读查询契约，与现有准入仓储隔离。
- `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductQueryService.java`：产品列表与详情查询规则。
- `corp-deposit-rag-server/deposit-product-service/src/test/java/com/ljp/corpdeposit/product/ProductQueryServiceTest.java`：产品查询单元测试。
- `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionSummary.java`：意向列表响应。
- `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionLineView.java`：意向明细响应。
- `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionDetailView.java`：意向详情响应。

### 后端修改文件

- `customer-service/.../CustomerProfileRepository.java`、`CustomerProfileMapper.java`、`CustomerProfileService.java`、`CustomerController.java`：增加启用客户列表。
- `customer-service/.../CustomerProfileServiceTest.java`、`CustomerControllerTest.java`：覆盖列表与详情。
- `deposit-product-service/.../ProductCatalogMapper.java`、`MyBatisProductCatalogRepository.java`、`ProductTermRow.java`、`ProductController.java`：增加产品列表、详情及期限查询；现有 `ProductCatalogRepository` 保持函数式准入契约。
- `deposit-business-service/.../IntentionRepository.java`、`IntentionMapper.java`、`MyBatisIntentionRepository.java`、`IntentionMasterRow.java`、`IntentionDetailRow.java`、`DepositIntentionService.java`、`DepositIntentionController.java`：增加意向筛选列表与详情。
- `deposit-business-service/.../DepositIntentionServiceTest.java`：覆盖查询与不存在错误。

### 前端新增文件

- `corp-deposit-rag-web/src/api/query.test.ts`：三个查询 API 的请求路径和 ID 适配测试。
- `corp-deposit-rag-web/src/utils/catalog.ts`：客户、产品、期限字典与缺失名称降级。
- `corp-deposit-rag-web/src/utils/catalog.test.ts`：聚合字典测试。

### 前端修改文件

- `corp-deposit-rag-web/src/api/customer.ts`：删除客户 Mock，调用客户服务。
- `corp-deposit-rag-web/src/api/product.ts`：调用产品列表/详情接口并适配 ID。
- `corp-deposit-rag-web/src/api/intention.ts`：调用意向列表/详情接口并适配 ID。
- `corp-deposit-rag-web/vite.config.ts`：增加三个服务代理。
- `corp-deposit-rag-web/src/views/product/index.vue`、`detail.vue`：真实产品列表和详情。
- `corp-deposit-rag-web/src/views/intention/index.vue`、`detail.vue`：真实意向筛选、列表、详情和名称聚合。
- `PROJECT_STATE.md`：记录 V1.3 完成事实与下一步。

### 保持不变

- `corp-deposit-rag-server/ai-assistant-service`：继续使用 Mock 需求提取器，不接入付费 API。
- `start-all.bat`、`corp-deposit-rag-server/scripts/start-all.ps1`：现有端口和启动顺序无需改变。

---

### Task 1: 客户列表接口

**Files:**
- Modify: `corp-deposit-rag-server/customer-service/src/main/java/com/ljp/corpdeposit/customer/CustomerProfileRepository.java`
- Modify: `corp-deposit-rag-server/customer-service/src/main/java/com/ljp/corpdeposit/customer/CustomerProfileMapper.java`
- Modify: `corp-deposit-rag-server/customer-service/src/main/java/com/ljp/corpdeposit/customer/CustomerProfileService.java`
- Modify: `corp-deposit-rag-server/customer-service/src/main/java/com/ljp/corpdeposit/customer/CustomerController.java`
- Test: `corp-deposit-rag-server/customer-service/src/test/java/com/ljp/corpdeposit/customer/CustomerProfileServiceTest.java`
- Test: `corp-deposit-rag-server/customer-service/src/test/java/com/ljp/corpdeposit/customer/CustomerControllerTest.java`

- [ ] **Step 1: 写失败的 Service 与 Controller 测试**

在现有测试中使用完整的匿名 Repository，明确断言排序后的列表可从 `GET /api/v1/customers` 返回：

```java
CustomerProfile first = new CustomerProfile("CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
CustomerProfile second = new CustomerProfile("CUST002", "海岳国有资本有限公司", 3, "国有企业", 301, "国有控股企业");
CustomerProfileRepository repository = new CustomerProfileRepository() {
    @Override public Optional<CustomerProfile> findByCustomerNo(String customerNo) { return Optional.of(first); }
    @Override public List<CustomerProfile> findAllActive() { return List.of(first, second); }
};
assertThat(new CustomerProfileService(repository).listActive()).containsExactly(first, second);

MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(new CustomerProfileService(repository))).build();
mvc.perform(get("/api/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].customerNo").value("CUST001"))
        .andExpect(jsonPath("$[1].customerNo").value("CUST002"));
```

- [ ] **Step 2: 运行客户服务测试并确认红灯**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl customer-service -am -Dtest=CustomerProfileServiceTest,CustomerControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL，提示 `findAllActive` 或 `listActive` 尚不存在。

- [ ] **Step 3: 实现最小客户列表查询**

Repository 增加：

```java
List<CustomerProfile> findAllActive();
```

Mapper 增加：

```java
@Override
@Select("""
        SELECT c.customer_no AS customerNo, c.customer_name AS customerName,
               lc.category_code AS largeCategoryCode, lc.category_name AS largeCategoryName,
               sc.category_code AS smallCategoryCode, sc.category_name AS smallCategoryName
          FROM customer c
          JOIN customer_small_category sc ON sc.sn_id = c.small_category_id AND sc.status = 1
          JOIN customer_large_category lc ON lc.sn_id = sc.large_category_id AND lc.status = 1
         WHERE c.status = 1
         ORDER BY c.customer_no
        """)
List<CustomerProfile> findAllActive();
```

同时给现有详情 SQL 的两个分类 JOIN 加上 `status = 1`。Service 与 Controller 分别增加：

```java
public List<CustomerProfile> listActive() { return repository.findAllActive(); }

@GetMapping
public List<CustomerProfile> listCustomers() { return customerProfileService.listActive(); }
```

- [ ] **Step 4: 运行客户服务测试并确认绿灯**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl customer-service -am -Dtest=CustomerProfileServiceTest,CustomerControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: PASS，客户列表与原详情用例全部通过。

- [ ] **Step 5: 提交客户接口**

```powershell
git add corp-deposit-rag-server/customer-service
git commit -m "feat: add active customer list API"
```

---

### Task 2: 产品列表与详情接口

**Files:**
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductRow.java`
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductSummary.java`
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductTermView.java`
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductDetail.java`
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductQueryRepository.java`
- Create: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductQueryService.java`
- Modify: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductCatalogMapper.java`
- Modify: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/MyBatisProductCatalogRepository.java`
- Modify: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductTermRow.java`
- Modify: `corp-deposit-rag-server/deposit-product-service/src/main/java/com/ljp/corpdeposit/product/ProductController.java`
- Test: `corp-deposit-rag-server/deposit-product-service/src/test/java/com/ljp/corpdeposit/product/ProductQueryServiceTest.java`

- [ ] **Step 1: 写产品查询失败测试**

测试 Repository Stub 返回一个产品及两个期限，并覆盖不存在产品：

```java
@Test
void returnsEnabledProductsAndActiveTerms() {
    ProductSummary summary = new ProductSummary(2L, "TIME", "单位定期存款", DepositType.TIME, 1);
    ProductTermView term = new ProductTermView(23L, "1Y", "一年", 365, 1_000_000L, 1_000_000L, null);
    ProductQueryRepository repository = new StubQueryRepository(List.of(summary), Optional.of(summary), List.of(term));
    ProductQueryService service = new ProductQueryService(repository);
    assertThat(service.listProducts()).containsExactly(summary);
    assertThat(service.getProduct(2L).terms()).containsExactly(term);
}

@Test
void rejectsMissingProduct() {
    ProductQueryRepository repository = new StubQueryRepository(List.of(), Optional.empty(), List.of());
    assertThatThrownBy(() -> new ProductQueryService(repository).getProduct(999L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("产品不存在: 999");
}
```

测试内部的 `StubQueryRepository` 完整实现如下：

```java
private record StubQueryRepository(List<ProductSummary> products,
                                   Optional<ProductSummary> product,
                                   List<ProductTermView> terms) implements ProductQueryRepository {
    @Override public List<ProductSummary> findAllActiveProducts() { return products; }
    @Override public Optional<ProductSummary> findActiveProduct(long productId) { return product; }
    @Override public List<ProductTermView> findActiveTerms(long productId) { return terms; }
}
```

- [ ] **Step 2: 运行产品查询测试并确认红灯**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl deposit-product-service -am -Dtest=ProductQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL，产品查询类型和方法尚不存在。

- [ ] **Step 3: 新增产品响应类型与查询服务**

```java
public record ProductSummary(Long productId, String productCode, String productName,
                             DepositType depositType, Integer status) {}

public record ProductTermView(Long productTermId, String termCode, String termName,
                              Integer termDays, Long minOpenAmountInCents,
                              Long minRetainAmountInCents, Integer noticeDays) {}

public record ProductDetail(Long productId, String productCode, String productName,
                            DepositType depositType, Integer status,
                            List<ProductTermView> terms) {}
```

`ProductQueryService` 的完整行为：

```java
@Service
public class ProductQueryService {
    private final ProductQueryRepository repository;
    public ProductQueryService(ProductQueryRepository repository) { this.repository = repository; }
    public List<ProductSummary> listProducts() { return repository.findAllActiveProducts(); }
    public ProductDetail getProduct(long productId) {
        ProductSummary product = repository.findActiveProduct(productId)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND", "产品不存在: " + productId, HttpStatus.NOT_FOUND));
        return new ProductDetail(product.productId(), product.productCode(), product.productName(),
                product.depositType(), product.status(), repository.findActiveTerms(productId));
    }
}
```

独立查询契约为：

```java
public interface ProductQueryRepository {
    List<ProductSummary> findAllActiveProducts();
    Optional<ProductSummary> findActiveProduct(long productId);
    List<ProductTermView> findActiveTerms(long productId);
}
```

- [ ] **Step 4: 实现 MyBatis 查询与映射**

`ProductRow` 保存 `productId/productCode/productName/depositType/status` 及标准 getter/setter。`ProductTermRow` 增加 `noticeDays`。Mapper 增加三条查询：

```java
@Select("SELECT sn_id AS productId, product_code AS productCode, product_name AS productName, deposit_type AS depositType, status FROM deposit_product WHERE status = 1 ORDER BY product_code")
List<ProductRow> findAllActiveProducts();

@Select("SELECT sn_id AS productId, product_code AS productCode, product_name AS productName, deposit_type AS depositType, status FROM deposit_product WHERE sn_id = #{productId} AND status = 1")
ProductRow findActiveProduct(@Param("productId") long productId);

@Select("SELECT sn_id AS productTermId, term_code AS termCode, term_name AS termName, term_days AS termDays, min_open_amount AS minOpenAmount, min_retain_amount AS minRetainAmount, notice_days AS noticeDays FROM deposit_product_term WHERE product_id = #{productId} AND status = 1 ORDER BY term_days, sn_id")
List<ProductTermRow> findActiveTerms(@Param("productId") long productId);
```

`MyBatisProductCatalogRepository` 同时实现原 `ProductCatalogRepository` 与新 `ProductQueryRepository`。查询方法将整数存款类型统一用现有 `depositType(int)` 转为 `DepositType`，并把金额字段映射为响应中的 `...InCents`；现有准入方法和函数式接口保持不变。

- [ ] **Step 5: 暴露产品 GET 接口**

给 `ProductController` 注入 `ProductQueryService` 并增加：

```java
@GetMapping
public List<ProductSummary> list() { return queryService.listProducts(); }

@GetMapping("/{productId}")
public ProductDetail detail(@PathVariable long productId) { return queryService.getProduct(productId); }
```

- [ ] **Step 6: 运行产品模块全部测试**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl deposit-product-service -am test`

Expected: PASS，原准入、利率、匹配测试无回归，新查询测试通过。

- [ ] **Step 7: 提交产品接口**

```powershell
git add corp-deposit-rag-server/deposit-product-service
git commit -m "feat: add product catalog query APIs"
```

---

### Task 3: 办理意向列表与详情接口

**Files:**
- Create: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionSummary.java`
- Create: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionLineView.java`
- Create: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionDetailView.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionRepository.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionMapper.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/MyBatisIntentionRepository.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionMasterRow.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/IntentionDetailRow.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/DepositIntentionService.java`
- Modify: `corp-deposit-rag-server/deposit-business-service/src/main/java/com/ljp/corpdeposit/business/DepositIntentionController.java`
- Test: `corp-deposit-rag-server/deposit-business-service/src/test/java/com/ljp/corpdeposit/business/DepositIntentionServiceTest.java`

- [ ] **Step 1: 写意向列表、详情和不存在失败测试**

扩展测试 Stub，使其实现创建、幂等、列表和详情查询，并断言 Service 原样传递筛选条件：

```java
IntentionSummary summary = new IntentionSummary("INT001", "CUST001", "800万存一年",
        800_000_000L, 1, 1, LocalDateTime.parse("2026-09-20T10:00:00"),
        LocalDateTime.parse("2026-09-20T10:00:00"));
IntentionLineView line = new IntentionLineView("DET001", 2L, 23L, "001",
        800_000_000L, 15_000L, 12_000_000L, 0,
        summary.createdAt(), summary.updatedAt());
IntentionDetailView detail = new IntentionDetailView(summary.intentionNo(), summary.customerNo(),
        summary.requirementText(), summary.totalAmountInCents(), summary.status(), summary.sourceChannel(),
        summary.createdAt(), summary.updatedAt(), List.of(line));

assertThat(service.list("CUST001", 1)).containsExactly(summary);
assertThat(service.get("INT001")).isEqualTo(detail);
assertThatThrownBy(() -> service.get("UNKNOWN"))
        .isInstanceOf(BusinessException.class)
        .hasMessage("办理意向不存在: UNKNOWN");
```

- [ ] **Step 2: 运行意向测试并确认红灯**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl deposit-business-service -am -Dtest=DepositIntentionServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL，查询响应类型与方法尚不存在。

- [ ] **Step 3: 新增意向响应记录与 Repository 契约**

```java
public record IntentionSummary(String intentionNo, String customerNo, String requirementText,
        Long totalAmountInCents, Integer status, Integer sourceChannel,
        LocalDateTime createdAt, LocalDateTime updatedAt) {}

public record IntentionLineView(String detailNo, Long productId, Long productTermId,
        String currencyCode, Long amountInCents, Long interestRate,
        Long expectedInterestInCents, Integer status,
        LocalDateTime createdAt, LocalDateTime updatedAt) {}

public record IntentionDetailView(String intentionNo, String customerNo, String requirementText,
        Long totalAmountInCents, Integer status, Integer sourceChannel,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        List<IntentionLineView> details) {}
```

Repository 增加：

```java
List<IntentionSummary> findAll(String customerNo, Integer status);
Optional<IntentionDetailView> findByIntentionNo(String intentionNo);
```

- [ ] **Step 4: 实现 MyBatis 行查询与组合**

给主行增加 `sourceChannel/createdAt/updatedAt`，给明细行增加 `status/createdAt/updatedAt`。Mapper 使用动态 SQL：

```java
@Select("""
        <script>
        SELECT sn_id, intention_no, customer_no, requirement_text, total_amount,
               status, source_channel, created_at, updated_at
          FROM deposit_intention
         <where>
           <if test='customerNo != null and customerNo != ""'>customer_no = #{customerNo}</if>
           <if test='status != null'>AND status = #{status}</if>
         </where>
         ORDER BY created_at DESC, sn_id DESC
        </script>
        """)
List<IntentionMasterRow> findAll(@Param("customerNo") String customerNo, @Param("status") Integer status);

@Select("SELECT sn_id, intention_no, customer_no, requirement_text, total_amount, status, source_channel, created_at, updated_at FROM deposit_intention WHERE intention_no = #{intentionNo}")
IntentionMasterRow findByIntentionNo(@Param("intentionNo") String intentionNo);

@Select("SELECT detail_no, product_id, product_term_id, currency_code, amount, interest_rate, expected_interest, status, created_at, updated_at FROM deposit_intention_detail WHERE intention_id = #{intentionId} ORDER BY sn_id")
List<IntentionDetailRow> findDetails(@Param("intentionId") long intentionId);
```

`MyBatisIntentionRepository.findAll` 映射主行；`findByIntentionNo` 在主单存在时查询明细并组装不可变列表。

- [ ] **Step 5: 实现 Service 错误和 Controller GET 路由**

```java
@Transactional(readOnly = true)
public List<IntentionSummary> list(String customerNo, Integer status) {
    return repository.findAll(customerNo, status);
}

@Transactional(readOnly = true)
public IntentionDetailView get(String intentionNo) {
    return repository.findByIntentionNo(intentionNo)
            .orElseThrow(() -> new BusinessException("INTENTION_NOT_FOUND",
                    "办理意向不存在: " + intentionNo, HttpStatus.NOT_FOUND));
}

@GetMapping
public List<IntentionSummary> list(@RequestParam(required = false) String customerNo,
                                   @RequestParam(required = false) Integer status) {
    return service.list(customerNo, status);
}

@GetMapping("/{intentionNo}")
public IntentionDetailView detail(@PathVariable String intentionNo) { return service.get(intentionNo); }
```

- [ ] **Step 6: 运行意向模块全部测试**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd -q -pl deposit-business-service -am test`

Expected: PASS，创建和幂等用例无回归，查询用例通过。

- [ ] **Step 7: 提交意向查询接口**

```powershell
git add corp-deposit-rag-server/deposit-business-service
git commit -m "feat: add intention query APIs"
```

---

### Task 4: 前端真实查询 API 与聚合字典

**Files:**
- Modify: `corp-deposit-rag-web/src/api/customer.ts`
- Modify: `corp-deposit-rag-web/src/api/product.ts`
- Modify: `corp-deposit-rag-web/src/api/intention.ts`
- Create: `corp-deposit-rag-web/src/api/query.test.ts`
- Create: `corp-deposit-rag-web/src/utils/catalog.ts`
- Create: `corp-deposit-rag-web/src/utils/catalog.test.ts`

- [ ] **Step 1: 写 API 路径、ID 适配和字典降级失败测试**

```ts
afterEach(() => vi.restoreAllMocks())

it('loads customers from customer service proxy', async () => {
  const get = vi.spyOn(request, 'get').mockResolvedValue({ data: [{ customerNo: 'CUST001' }] })
  await listCustomers()
  expect(get).toHaveBeenCalledWith('/customer-api/v1/customers')
})

it('adapts product and term ids to strings', async () => {
  vi.spyOn(request, 'get').mockResolvedValue({ data: { productId: 2, productCode: 'TIME', productName: '单位定期存款', depositType: 'TIME', status: 1, terms: [{ productTermId: 23, termCode: '1Y', termName: '一年', termDays: 365, minOpenAmountInCents: 1000000, minRetainAmountInCents: 1000000 }] } })
  const product = await getProduct('2')
  expect(product.productId).toBe('2')
  expect(product.terms[0]?.productTermId).toBe('23')
})

it('preserves raw ids when catalog names are missing', () => {
  const catalog = createCatalog([], [])
  expect(catalog.customerName('CUST404')).toBe('CUST404')
  expect(catalog.productName('999')).toBe('999')
  expect(catalog.termName('999')).toBe('999')
})
```

- [ ] **Step 2: 运行 Vitest 并确认红灯**

Run: `cd corp-deposit-rag-web && npm test -- --run src/api/query.test.ts src/utils/catalog.test.ts`

Expected: FAIL，真实 API 和字典函数尚不存在。

- [ ] **Step 3: 实现客户、产品、意向 API**

客户 API：

```ts
export async function listCustomers(): Promise<CustomerSummary[]> {
  const { data } = await request.get<CustomerSummary[]>('/customer-api/v1/customers')
  return data
}
```

产品类型与适配：

```ts
export interface ProductTerm { productTermId: string; termCode: string; termName: string; termDays: number; minOpenAmountInCents: number; minRetainAmountInCents: number; noticeDays?: number }
export interface ProductListItem { productId: string; productCode: string; productName: string; depositType: string; status: number }
export interface ProductDetail extends ProductListItem { terms: ProductTerm[] }
const adaptProduct = <T extends { productId: string | number }>(raw: T) => ({ ...raw, productId: String(raw.productId) })
export async function listProducts(): Promise<ProductListItem[]> {
  const { data } = await request.get<Array<Omit<ProductListItem, 'productId'> & { productId: string | number }>>('/product-api/v1/products')
  return data.map(adaptProduct)
}
export async function getProduct(productId: string): Promise<ProductDetail> {
  const { data } = await request.get<Omit<ProductDetail, 'productId' | 'terms'> & { productId: string | number; terms: Array<Omit<ProductTerm, 'productTermId'> & { productTermId: string | number }> }>(`/product-api/v1/products/${productId}`)
  return { ...data, productId: String(data.productId), terms: data.terms.map(term => ({ ...term, productTermId: String(term.productTermId) })) }
}
```

意向 API 定义 `IntentionListItem` 和 `IntentionDetail`，金额/利率保持 number，两个 ID 转字符串；列表用 `URLSearchParams` 只发送实际填写的 `customerNo/status`，请求 `/business-api/v1/intentions`，详情请求 `/business-api/v1/intentions/${intentionNo}`。

- [ ] **Step 4: 实现聚合字典**

```ts
export function createCatalog(customers: CustomerSummary[], products: ProductDetail[]) {
  const customerNames = new Map(customers.map(item => [item.customerNo, item.customerName]))
  const productNames = new Map(products.map(item => [item.productId, item.productName]))
  const termNames = new Map(products.flatMap(item => item.terms.map(term => [term.productTermId, term.termName] as const)))
  return {
    customerName: (customerNo: string) => customerNames.get(customerNo) ?? customerNo,
    productName: (productId: string) => productNames.get(productId) ?? productId,
    termName: (productTermId: string) => termNames.get(productTermId) ?? productTermId
  }
}
```

- [ ] **Step 5: 运行前端查询单测并确认绿灯**

Run: `cd corp-deposit-rag-web && npm test -- --run src/api/query.test.ts src/utils/catalog.test.ts`

Expected: PASS，三个代理路径、ID 字符串适配、错误透传和缺失名称降级全部通过。

- [ ] **Step 6: 提交前端数据层**

```powershell
git add corp-deposit-rag-web/src/api corp-deposit-rag-web/src/utils
git commit -m "feat: connect frontend query APIs"
```

---

### Task 5: Vite 代理和产品页面

**Files:**
- Modify: `corp-deposit-rag-web/vite.config.ts`
- Modify: `corp-deposit-rag-web/src/views/product/index.vue`
- Modify: `corp-deposit-rag-web/src/views/product/detail.vue`

- [ ] **Step 1: 配置三个服务代理**

在现有 `/api` 代理之前增加：

```ts
'/customer-api': {
  target: env.VITE_CUSTOMER_API || 'http://127.0.0.1:8081',
  changeOrigin: true,
  rewrite: path => path.replace(/^\/customer-api/, '/api')
},
'/product-api': {
  target: env.VITE_PRODUCT_API || 'http://127.0.0.1:8082',
  changeOrigin: true,
  rewrite: path => path.replace(/^\/product-api/, '/api')
},
'/business-api': {
  target: env.VITE_BUSINESS_API || 'http://127.0.0.1:8083',
  changeOrigin: true,
  rewrite: path => path.replace(/^\/business-api/, '/api')
}
```

- [ ] **Step 2: 实现产品列表交互**

列表页在 `onMounted` 调用 `listProducts()`；增加 loading/error 状态；通过 `router.push('/products/' + row.productId)` 进入详情；使用固定映射显示存款类型：`DEMAND -> 活期`、`TIME -> 定期`、`NOTICE -> 通知`、`LARGE_CERTIFICATE -> 大额存单`；状态 `1` 显示“启用”。

- [ ] **Step 3: 实现产品详情**

详情页读取 `route.params.id` 调用 `getProduct`，头部显示编码、名称、类型；期限表显示期限名称、天数、`formatMoneyShort(minOpenAmountInCents)`、`formatMoneyShort(minRetainAmountInCents)` 和通知天数。请求失败显示 `el-alert` 的错误消息，不使用 Mock 或静默兜底。

- [ ] **Step 4: 运行 TypeScript 构建**

Run: `cd corp-deposit-rag-web && npm run build`

Expected: PASS，`vue-tsc` 和 Vite 构建完成且无类型错误。

- [ ] **Step 5: 提交产品页面**

```powershell
git add corp-deposit-rag-web/vite.config.ts corp-deposit-rag-web/src/views/product
git commit -m "feat: render live product catalog"
```

---

### Task 6: 意向列表与详情页面

**Files:**
- Modify: `corp-deposit-rag-web/src/views/intention/index.vue`
- Modify: `corp-deposit-rag-web/src/views/intention/detail.vue`

- [ ] **Step 1: 实现列表筛选和名称聚合**

页面初始化并行调用 `listCustomers()`、`listProducts()` 和各产品 `getProduct()`，建立 `createCatalog` 字典；查询按钮把客户编号和数字状态传给 `listIntentions`。表格显示意向编号、客户名称、`formatMoneyShort(totalAmountInCents)`、状态文字和创建时间，行点击进入 `/intentions/${intentionNo}`。状态映射固定为：`0 草稿、1 已确认、2 处理中、3 部分成功、4 成功、5 失败、6 已取消`。

- [ ] **Step 2: 实现意向详情聚合**

详情页并行加载当前意向、客户和产品详情，主卡展示编号、客户、需求、总金额、状态、来源渠道和时间；明细表展示明细编号、产品、期限、币种、金额、`formatRate(interestRate)`、预期收益和明细状态。明细状态映射固定为：`0 待处理、1 处理中、2 成功、3 失败、4 已取消`；无法补全名称时字典返回原始 ID。

- [ ] **Step 3: 运行前端全量测试和构建**

Run: `cd corp-deposit-rag-web && npm test`

Expected: PASS，原 9 项测试与新增查询/聚合测试全部通过。

Run: `cd corp-deposit-rag-web && npm run build`

Expected: PASS，无 TypeScript 或模板编译错误。

- [ ] **Step 4: 提交意向页面**

```powershell
git add corp-deposit-rag-web/src/views/intention
git commit -m "feat: render live intention queries"
```

---

### Task 7: 全链路验证、项目状态与远端同步

**Files:**
- Modify: `PROJECT_STATE.md`

- [ ] **Step 1: 运行后端全量测试与打包**

Run: `cd corp-deposit-rag-server && .\mvnw.cmd clean package`

Expected: BUILD SUCCESS；原 12 项测试与新增查询测试全部通过，四个可运行 JAR 生成。

- [ ] **Step 2: 运行前端全量验证**

Run: `cd corp-deposit-rag-web && npm test && npm run build`

Expected: 全部 Vitest 通过，生产构建成功。

- [ ] **Step 3: 运行一键启动和 HTTP 冒烟**

从仓库根目录运行 `start-all.bat`，等待五个端口就绪。验证：

```powershell
Invoke-RestMethod http://127.0.0.1:5173/customer-api/v1/customers
Invoke-RestMethod http://127.0.0.1:5173/product-api/v1/products
Invoke-RestMethod http://127.0.0.1:5173/product-api/v1/products/2
Invoke-RestMethod http://127.0.0.1:5173/business-api/v1/intentions
```

Expected: 客户列表含 `CUST001/CUST002`；产品列表含四类产品；产品 2 含启用期限；意向列表返回数组。随后在 `/assistant` 创建一笔意向，确认 `/intentions` 和 `/intentions/{intentionNo}` 显示同一笔真实数据。

- [ ] **Step 4: 更新 PROJECT_STATE.md**

新增 `V1.3 查询接口与真实数据页面已完成`，记录六个 GET 接口、前端三服务代理、BIGINT 字符串适配、聚合降级、测试数量与一键启动结果；“下一步”调整为 Gateway/JWT、定期存款模拟开户、RAG/真实 LLM 后置。

- [ ] **Step 5: 提交文档并做最终差异检查**

```powershell
git add PROJECT_STATE.md
git commit -m "docs: record query API milestone"
git status --short
git diff main...HEAD --check
```

Expected: 工作树干净，`git diff --check` 无输出。

- [ ] **Step 6: 推送当前功能分支**

```powershell
git push -u origin feature/query-apis
```

Expected: GitHub 上出现 `feature/query-apis`，包含设计、实现、测试与项目状态提交；未经用户明确要求不自动合并到 `main`。
