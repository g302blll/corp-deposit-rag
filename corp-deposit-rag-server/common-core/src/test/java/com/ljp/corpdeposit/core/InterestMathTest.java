package com.ljp.corpdeposit.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InterestMathTest {

    @Test
    void convertsScaledRateToDecimal() {
        assertThat(InterestMath.toDecimalRate(15_000L))
                .isEqualByComparingTo(new BigDecimal("0.015"));
    }

    @Test
    void calculatesInterestInCentsWithHalfUpRounding() {
        long interest = InterestMath.simpleInterestInCents(500_000_000L, 15_000L, 365);

        assertThat(interest).isEqualTo(7_500_000L);
    }

    @Test
    void preservesCentPrecisionForPartialYear() {
        long interest = InterestMath.simpleInterestInCents(1_000_001L, 13_500L, 30);

        assertThat(interest).isEqualTo(1_110L);
    }
}

