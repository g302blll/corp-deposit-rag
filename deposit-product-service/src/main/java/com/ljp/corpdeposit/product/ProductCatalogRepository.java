package com.ljp.corpdeposit.product;

import java.util.List;

@FunctionalInterface
public interface ProductCatalogRepository {

    List<ProductTermCandidate> findEligibleTerms(int smallCategoryCode);
}

