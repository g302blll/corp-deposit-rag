package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;

import java.util.List;

public record ProductDetail(
        Long productId,
        String productCode,
        String productName,
        DepositType depositType,
        Integer status,
        List<ProductTermView> terms) {
}
