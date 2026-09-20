package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
import com.ljp.corpdeposit.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    @Test
    void listsActiveProducts() throws Exception {
        ProductSummary product = new ProductSummary(
                2L, "TIME", "单位定期存款", DepositType.TIME, 1);
        MockMvc mvc = mvc(repository(List.of(product), Optional.empty(), List.of()));

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2L))
                .andExpect(jsonPath("$[0].productCode").value("TIME"));
    }

    @Test
    void serializesProductIdAsStringWhenListingProducts() throws Exception {
        long unsafeJavaScriptInteger = 9_007_199_254_740_993L;
        ProductSummary product = new ProductSummary(
                unsafeJavaScriptInteger, "TIME", "单位定期存款", DepositType.TIME, 1);
        MockMvc mvc = mvc(repository(List.of(product), Optional.empty(), List.of()));

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(is("9007199254740993")))
                .andExpect(jsonPath("$[0].status").value(is(1)));
    }

    @Test
    void returnsProductDetail() throws Exception {
        ProductSummary product = new ProductSummary(
                3L, "NOTICE", "单位通知存款", DepositType.NOTICE, 1);
        ProductTermView term = new ProductTermView(
                32L, "7D", "七天通知", 7, 5_000_000L, 5_000_000L, 7);
        MockMvc mvc = mvc(repository(List.of(product), Optional.of(product), List.of(term)));

        mvc.perform(get("/api/v1/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(3L))
                .andExpect(jsonPath("$.terms[0].noticeDays").value(7));
    }

    @Test
    void serializesProductAndTermIdsAsStringsWithoutChangingNumericFields() throws Exception {
        long unsafeJavaScriptInteger = 9_007_199_254_740_993L;
        ProductSummary product = new ProductSummary(
                unsafeJavaScriptInteger, "NOTICE", "单位通知存款", DepositType.NOTICE, 1);
        ProductTermView term = new ProductTermView(
                unsafeJavaScriptInteger, "7D", "七天通知", 7,
                5_000_000L, 4_000_000L, 7);
        MockMvc mvc = mvc(repository(
                List.of(product), Optional.of(product), List.of(term)));

        mvc.perform(get("/api/v1/products/" + unsafeJavaScriptInteger))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(is("9007199254740993")))
                .andExpect(jsonPath("$.terms[0].productTermId").value(is("9007199254740993")))
                .andExpect(jsonPath("$.terms[0].minOpenAmountInCents").value(is(5_000_000)))
                .andExpect(jsonPath("$.terms[0].minRetainAmountInCents").value(is(4_000_000)))
                .andExpect(jsonPath("$.terms[0].termDays").value(is(7)))
                .andExpect(jsonPath("$.terms[0].noticeDays").value(is(7)))
                .andExpect(jsonPath("$.status").value(is(1)));
    }

    @Test
    void returnsProductNotFoundErrorCode() throws Exception {
        MockMvc mvc = mvc(repository(List.of(), Optional.empty(), List.of()));

        mvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void keepsEligibleRouteCompatibleWithProductIdRoute() throws Exception {
        ProductTermCandidate term = new ProductTermCandidate(
                2L, "TIME", "单位定期存款", DepositType.TIME,
                21L, "3M", "三个月", 90, 1_000_000L, 1_000_000L);
        MockMvc mvc = mvc(repository(List.of(), Optional.empty(), List.of()), code -> List.of(term));

        mvc.perform(get("/api/v1/products/eligible").param("smallCategoryCode", "401"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productTermId").value(21L));
    }

    private MockMvc mvc(ProductQueryRepository repository) {
        return mvc(repository, code -> List.of());
    }

    private MockMvc mvc(ProductQueryRepository repository, ProductCatalogRepository catalogRepository) {
        ProductEligibilityService eligibilityService = new ProductEligibilityService(catalogRepository);
        ProductRateService rateService = new ProductRateService(query -> Optional.empty());
        ProductMatchService matchService = new ProductMatchService(
                eligibilityService, rateService, new InterestCalculationService());
        ProductController controller = new ProductController(
                new ProductQueryService(repository), eligibilityService, rateService, matchService);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductQueryRepository repository(List<ProductSummary> products,
                                              Optional<ProductSummary> product,
                                              List<ProductTermView> terms) {
        return new ProductQueryRepository() {
            @Override
            public List<ProductSummary> findAllActiveProducts() {
                return products;
            }

            @Override
            public Optional<ProductSummary> findActiveProduct(long productId) {
                return product;
            }

            @Override
            public List<ProductTermView> findActiveTerms(long productId) {
                return terms;
            }
        };
    }
}
