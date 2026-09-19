package com.ljp.corpdeposit.product;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductRateService {

    private final ProductRateRepository repository;

    public ProductRateService(ProductRateRepository repository) {
        this.repository = repository;
    }

    public Optional<Long> findEffectiveRate(RateQuery query) {
        return repository.findEffectiveRate(query);
    }
}

