package com.ljp.corpdeposit.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class InterestMath {

    public static final BigDecimal RATE_SCALE = BigDecimal.valueOf(1_000_000L);
    private static final BigDecimal DAYS_PER_YEAR = BigDecimal.valueOf(365L);

    private InterestMath() {
    }

    public static BigDecimal toDecimalRate(long scaledRate) {
        if (scaledRate < 0) {
            throw new IllegalArgumentException("scaledRate must not be negative");
        }
        return BigDecimal.valueOf(scaledRate).divide(RATE_SCALE);
    }

    public static long simpleInterestInCents(long principalInCents, long scaledRate, int actualDays) {
        if (principalInCents < 0) {
            throw new IllegalArgumentException("principalInCents must not be negative");
        }
        if (actualDays < 0) {
            throw new IllegalArgumentException("actualDays must not be negative");
        }
        return BigDecimal.valueOf(principalInCents)
                .multiply(toDecimalRate(scaledRate))
                .multiply(BigDecimal.valueOf(actualDays))
                .divide(DAYS_PER_YEAR, 0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}

