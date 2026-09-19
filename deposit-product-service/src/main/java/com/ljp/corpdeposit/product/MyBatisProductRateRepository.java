package com.ljp.corpdeposit.product;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisProductRateRepository implements ProductRateRepository {

    private final ProductRateMapper mapper;

    public MyBatisProductRateRepository(ProductRateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Long> findEffectiveRate(RateQuery query) {
        return Optional.ofNullable(mapper.findEffectiveRate(query));
    }
}

