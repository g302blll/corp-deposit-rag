package com.ljp.corpdeposit.core;

public record CustomerProfile(
        String customerNo,
        String customerName,
        int largeCategoryCode,
        String largeCategoryName,
        int smallCategoryCode,
        String smallCategoryName) {
}

