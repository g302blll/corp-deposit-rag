package com.ljp.corpdeposit.product;

public record ProductTermView(
        Long productTermId,
        String termCode,
        String termName,
        Integer termDays,
        Long minOpenAmountInCents,
        Long minRetainAmountInCents,
        Integer noticeDays) {
}
