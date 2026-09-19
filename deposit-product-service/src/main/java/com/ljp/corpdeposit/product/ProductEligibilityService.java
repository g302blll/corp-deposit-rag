package com.ljp.corpdeposit.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductEligibilityService {

    private final ProductCatalogRepository repository;

    public ProductEligibilityService(ProductCatalogRepository repository) {
        this.repository = repository;
    }

    public List<ProductTermCandidate> findEligibleTerms(int smallCategoryCode) {
        return repository.findEligibleTerms(smallCategoryCode);
    }
}

