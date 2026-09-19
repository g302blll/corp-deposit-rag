package com.ljp.corpdeposit.core;

public record IntentionDetailCommand(
        long productId,
        long productTermId,
        long amountInCents,
        long interestRate,
        long expectedInterestInCents,
        String currencyCode) {
}

