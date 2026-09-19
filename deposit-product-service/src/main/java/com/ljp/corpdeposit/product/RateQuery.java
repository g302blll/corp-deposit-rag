package com.ljp.corpdeposit.product;

import java.time.LocalDate;

public record RateQuery(
        long productId,
        long productTermId,
        int largeCategoryCode,
        int smallCategoryCode,
        String currencyCode,
        LocalDate businessDate) {
}

