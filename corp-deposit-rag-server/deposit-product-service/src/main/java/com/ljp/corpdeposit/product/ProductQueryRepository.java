package com.ljp.corpdeposit.product;

import java.util.List;
import java.util.Optional;

public interface ProductQueryRepository {

    List<ProductSummary> findAllActiveProducts();

    Optional<ProductSummary> findActiveProduct(long productId);

    List<ProductTermView> findActiveTerms(long productId);
}
