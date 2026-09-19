package com.ljp.corpdeposit.product;

import java.util.Optional;

@FunctionalInterface
public interface ProductRateRepository {

    Optional<Long> findEffectiveRate(RateQuery query);
}

