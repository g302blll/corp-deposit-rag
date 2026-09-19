package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.core.DepositRequirement;
import com.ljp.corpdeposit.core.DepositType;
import com.ljp.corpdeposit.core.LiquidityPreference;
import com.ljp.corpdeposit.core.ProductPlan;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiOrchestratorServiceTest {

    @Test
    void orchestratesCustomerThenProductMatchAndBuildsExplanation() {
        CustomerProfile profile = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        ProductPlan plan = new ProductPlan(
                2L, "TIME", "单位定期存款", DepositType.TIME, 23L, "1Y", "一年",
                365, 800_000_000L, 15_000L, 12_000_000L, "001");
        DepositRequirement requirement = new DepositRequirement(
                800_000_000L, 365, LiquidityPreference.LOW, "001");
        RequirementExtractor extractor = text -> requirement;
        CustomerTool customerTool = customerNo -> profile;
        ProductTool productTool = request -> List.of(plan);
        AiOrchestratorService service = new AiOrchestratorService(
                extractor, customerTool, productTool,
                Clock.fixed(Instant.parse("2026-09-19T00:00:00Z"), ZoneOffset.UTC));

        RecommendationResponse response = service.recommend("CUST001", "800万存一年");

        assertThat(response.customer()).isEqualTo(profile);
        assertThat(response.requirement()).isEqualTo(requirement);
        assertThat(response.plans()).containsExactly(plan);
        assertThat(response.explanation()).contains("单位定期存款", "预计收益120000.00元");
    }
}

