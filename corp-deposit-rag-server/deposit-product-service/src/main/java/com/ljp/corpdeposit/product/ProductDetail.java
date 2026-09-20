package com.ljp.corpdeposit.product;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ljp.corpdeposit.core.DepositType;

import java.util.List;

public record ProductDetail(
        @JsonSerialize(using = ToStringSerializer.class) Long productId,
        String productCode,
        String productName,
        DepositType depositType,
        Integer status,
        List<ProductTermView> terms) {
}
