package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.core.DepositRequirement;
import com.ljp.corpdeposit.core.ProductPlan;

import java.util.List;

public record RecommendationResponse(
        CustomerProfile customer,
        DepositRequirement requirement,
        List<ProductPlan> plans,
        String explanation) {
}

