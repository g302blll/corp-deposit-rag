package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisProductCatalogRepository implements ProductCatalogRepository, ProductQueryRepository {

    private final ProductCatalogMapper mapper;

    public MyBatisProductCatalogRepository(ProductCatalogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProductTermCandidate> findEligibleTerms(int smallCategoryCode) {
        return mapper.findEligibleTerms(smallCategoryCode).stream()
                .map(row -> new ProductTermCandidate(
                        row.getProductId(), row.getProductCode(), row.getProductName(),
                        depositType(row.getDepositType()), row.getProductTermId(), row.getTermCode(),
                        row.getTermName(), row.getTermDays(), row.getMinOpenAmount(), row.getMinRetainAmount()))
                .toList();
    }

    @Override
    public List<ProductSummary> findAllActiveProducts() {
        return mapper.findAllActiveProducts().stream()
                .map(this::productSummary)
                .toList();
    }

    @Override
    public Optional<ProductSummary> findActiveProduct(long productId) {
        return Optional.ofNullable(mapper.findActiveProduct(productId))
                .map(this::productSummary);
    }

    @Override
    public List<ProductTermView> findActiveTerms(long productId) {
        return mapper.findActiveTerms(productId).stream()
                .map(row -> new ProductTermView(
                        row.getProductTermId(), row.getTermCode(), row.getTermName(),
                        row.getTermDays(), row.getMinOpenAmount(), row.getMinRetainAmount(),
                        row.getNoticeDays()))
                .toList();
    }

    private ProductSummary productSummary(ProductRow row) {
        return new ProductSummary(
                row.getProductId(), row.getProductCode(), row.getProductName(),
                depositType(row.getDepositType()), row.getStatus());
    }

    private DepositType depositType(int code) {
        return Arrays.stream(DepositType.values())
                .filter(type -> type.code() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未知存款类型: " + code));
    }
}

