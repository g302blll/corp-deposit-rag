package com.ljp.corpdeposit.product;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ljp.corpdeposit.core.DepositType;

public record ProductSummary(
        @JsonSerialize(using = ToStringSerializer.class) Long productId,
        String productCode,
        String productName,
        DepositType depositType,
        Integer status) {
}
