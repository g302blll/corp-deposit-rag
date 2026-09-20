package com.ljp.corpdeposit.business;

import java.time.LocalDateTime;

public record IntentionLineView(
        String detailNo,
        Long productId,
        Long productTermId,
        String currencyCode,
        Long amountInCents,
        Long interestRate,
        Long expectedInterestInCents,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
