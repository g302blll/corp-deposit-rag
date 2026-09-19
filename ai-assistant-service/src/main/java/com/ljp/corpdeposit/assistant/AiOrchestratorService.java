package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.core.DepositRequirement;
import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiOrchestratorService {
    private final RequirementExtractor extractor;
    private final CustomerTool customerTool;
    private final ProductTool productTool;
    private final Clock clock;

    public AiOrchestratorService(RequirementExtractor extractor, CustomerTool customerTool,
                                 ProductTool productTool, Clock clock) {
        this.extractor = extractor;
        this.customerTool = customerTool;
        this.productTool = productTool;
        this.clock = clock;
    }

    public RecommendationResponse recommend(String customerNo, String text) {
        DepositRequirement requirement = extractor.extract(text);
        CustomerProfile customer = customerTool.getCustomerProfile(customerNo);
        ProductMatchRequest request = new ProductMatchRequest(
                customer.largeCategoryCode(), customer.smallCategoryCode(), requirement.amountInCents(),
                requirement.preferredDays(), requirement.liquidityPreference(), requirement.currencyCode(),
                LocalDate.now(clock));
        List<ProductPlan> plans = productTool.match(request);
        return new RecommendationResponse(customer, requirement, plans, explain(plans));
    }

    private String explain(List<ProductPlan> plans) {
        if (plans.isEmpty()) return "未找到同时满足准入、金额和有效利率条件的方案。";
        return plans.stream().map(plan -> plan.productName() + "（" + plan.termName() + "），预计收益"
                        + BigDecimal.valueOf(plan.expectedInterestInCents(), 2)
                        .setScale(2, RoundingMode.UNNECESSARY).toPlainString() + "元")
                .collect(Collectors.joining("；"));
    }
}

