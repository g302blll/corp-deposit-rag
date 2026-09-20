package com.ljp.corpdeposit.business;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public record IntentionLineView(
        String detailNo,
        @JsonSerialize(using = ToStringSerializer.class) Long productId,
        @JsonSerialize(using = ToStringSerializer.class) Long productTermId,
        String currencyCode,
        Long amountInCents,
        Long interestRate,
        Long expectedInterestInCents,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
