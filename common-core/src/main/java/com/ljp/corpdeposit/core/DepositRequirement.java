package com.ljp.corpdeposit.core;

public record DepositRequirement(
        long amountInCents,
        Integer preferredDays,
        LiquidityPreference liquidityPreference,
        String currencyCode) {
}

