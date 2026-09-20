package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
import com.ljp.corpdeposit.web.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductQueryServiceTest {

    @Test
    void returnsActiveProducts() {
        ProductSummary demand = new ProductSummary(
                1L, "DEMAND", "单位活期存款", DepositType.DEMAND, 1);
        ProductSummary time = new ProductSummary(
                2L, "TIME", "单位定期存款", DepositType.TIME, 1);
        ProductQueryService service = new ProductQueryService(repository(
                List.of(demand, time), Optional.empty(), List.of()));

        assertThat(service.listProducts()).containsExactly(demand, time);
    }

    @Test
    void returnsProductDetailWithAllActiveTerms() {
        ProductSummary product = new ProductSummary(
                3L, "NOTICE", "单位通知存款", DepositType.NOTICE, 1);
        ProductTermView oneDay = new ProductTermView(
                31L, "1D", "一天通知", 1, 500_000L, 500_000L, 1);
        ProductTermView sevenDays = new ProductTermView(
                32L, "7D", "七天通知", 7, 500_000L, 500_000L, 7);
        List<ProductTermView> repositoryTerms = new ArrayList<>(List.of(oneDay, sevenDays));
        ProductQueryService service = new ProductQueryService(repository(
                List.of(product), Optional.of(product), repositoryTerms));

        ProductDetail detail = service.getProduct(3L);

        assertThat(detail).isEqualTo(new ProductDetail(
                3L, "NOTICE", "单位通知存款", DepositType.NOTICE, 1,
                List.of(oneDay, sevenDays)));
        repositoryTerms.clear();
        assertThat(detail.terms()).containsExactly(oneDay, sevenDays);
        assertThatThrownBy(() -> detail.terms().add(oneDay))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMissingProduct() {
        ProductQueryService service = new ProductQueryService(repository(
                List.of(), Optional.empty(), List.of()));

        assertThatThrownBy(() -> service.getProduct(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("PRODUCT_NOT_FOUND");
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception).hasMessage("产品不存在: 999");
                });
    }

    private ProductQueryRepository repository(List<ProductSummary> products,
                                              Optional<ProductSummary> product,
                                              List<ProductTermView> terms) {
        return new ProductQueryRepository() {
            @Override
            public List<ProductSummary> findAllActiveProducts() {
                return products;
            }

            @Override
            public Optional<ProductSummary> findActiveProduct(long productId) {
                return product;
            }

            @Override
            public List<ProductTermView> findActiveTerms(long productId) {
                return terms;
            }
        };
    }
}
