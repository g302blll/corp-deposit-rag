package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class MyBatisProductCatalogRepository implements ProductCatalogRepository {

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

    private DepositType depositType(int code) {
        return Arrays.stream(DepositType.values())
                .filter(type -> type.code() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未知存款类型: " + code));
    }
}

