package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.web.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductQueryRepository repository;

    public ProductQueryService(ProductQueryRepository repository) {
        this.repository = repository;
    }

    public List<ProductSummary> listProducts() {
        return repository.findAllActiveProducts();
    }

    public ProductDetail getProduct(long productId) {
        ProductSummary product = repository.findActiveProduct(productId)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND", "产品不存在: " + productId, HttpStatus.NOT_FOUND));
        return new ProductDetail(
                product.productId(),
                product.productCode(),
                product.productName(),
                product.depositType(),
                product.status(),
                List.copyOf(repository.findActiveTerms(productId)));
    }
}
