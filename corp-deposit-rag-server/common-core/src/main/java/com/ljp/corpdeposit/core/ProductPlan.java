package com.ljp.corpdeposit.core;

public record ProductPlan(
        long productId,
        String productCode,
        String productName,
        DepositType depositType,
        long productTermId,
        String termCode,
        String termName,
        int termDays,
        long principalInCents,
        long interestRate,
        long expectedInterestInCents,
        String currencyCode) {
}

