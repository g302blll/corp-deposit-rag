package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;

public record ProductSummary(
        Long productId,
        String productCode,
        String productName,
        DepositType depositType,
        Integer status) {
}
