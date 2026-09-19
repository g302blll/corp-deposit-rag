package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;

import java.util.List;

@FunctionalInterface
public interface ProductTool {
    List<ProductPlan> match(ProductMatchRequest request);
}

