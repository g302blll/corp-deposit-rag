package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;

public record ProductTermCandidate(
        long productId,
        String productCode,
        String productName,
        DepositType depositType,
        long productTermId,
        String termCode,
        String termName,
        int termDays,
        long minOpenAmount,
        long minRetainAmount) {
}

