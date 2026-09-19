package com.ljp.corpdeposit.core;

import java.time.LocalDate;

public record ProductMatchRequest(
        int largeCategoryCode,
        int smallCategoryCode,
        long amountInCents,
        Integer preferredDays,
        LiquidityPreference liquidityPreference,
        String currencyCode,
        LocalDate businessDate) {
}

