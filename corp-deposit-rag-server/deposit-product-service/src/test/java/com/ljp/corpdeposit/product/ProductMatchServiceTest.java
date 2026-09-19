package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
import com.ljp.corpdeposit.core.LiquidityPreference;
import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMatchServiceTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 9, 19);

    @Test
    void appliesEligibilityMinimumAmountRateAndInterestRules() {
        ProductTermCandidate time = new ProductTermCandidate(
                2L, "TIME", "单位定期存款", DepositType.TIME,
                21L, "1Y", "一年", 365, 1_000_000L, 1_000_000L);
        ProductTermCandidate large = new ProductTermCandidate(
                4L, "LARGE", "单位大额存单", DepositType.LARGE_CERTIFICATE,
                41L, "1Y", "一年", 365, 20_000_000L, 20_000_000L);
        ProductEligibilityService eligibility = new ProductEligibilityService(
                smallCategoryCode -> List.of(time, large));
        ProductRateService rates = new ProductRateService(query ->
                query.productId() == 2L ? Optional.of(15_000L) : Optional.of(20_000L));
        ProductMatchService service = new ProductMatchService(
                eligibility, rates, new InterestCalculationService());
        ProductMatchRequest request = new ProductMatchRequest(
                4, 401, 8_000_000L, 365, LiquidityPreference.LOW, "001", BUSINESS_DATE);

        List<ProductPlan> plans = service.match(request);

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).productCode()).isEqualTo("TIME");
        assertThat(plans.get(0).interestRate()).isEqualTo(15_000L);
        assertThat(plans.get(0).expectedInterestInCents()).isEqualTo(120_000L);
    }

    @Test
    void excludesEligibleTermWhenNoEffectiveRateExists() {
        ProductTermCandidate time = new ProductTermCandidate(
                2L, "TIME", "单位定期存款", DepositType.TIME,
                21L, "1Y", "一年", 365, 1_000_000L, 1_000_000L);
        ProductMatchService service = new ProductMatchService(
                new ProductEligibilityService(code -> List.of(time)),
                new ProductRateService(query -> Optional.empty()),
                new InterestCalculationService());
        ProductMatchRequest request = new ProductMatchRequest(
                4, 401, 8_000_000L, 365, LiquidityPreference.LOW, "001", BUSINESS_DATE);

        assertThat(service.match(request)).isEmpty();
    }
}

