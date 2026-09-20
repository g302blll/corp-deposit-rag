package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.web.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerProfileServiceTest {

    @Test
    void returnsCustomerWithLargeAndSmallCategory() {
        CustomerProfile expected = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        CustomerProfileService service = new CustomerProfileService(new CustomerProfileRepository() {
            @Override
            public Optional<CustomerProfile> findByCustomerNo(String customerNo) {
                return Optional.of(expected);
            }

            @Override
            public List<CustomerProfile> findAllActive() {
                return List.of(expected);
            }
        });

        CustomerProfile actual = service.getByCustomerNo("CUST001");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void rejectsUnknownCustomerNumber() {
        CustomerProfileService service = new CustomerProfileService(new CustomerProfileRepository() {
            @Override
            public Optional<CustomerProfile> findByCustomerNo(String customerNo) {
                return Optional.empty();
            }

            @Override
            public List<CustomerProfile> findAllActive() {
                return List.of();
            }
        });

        assertThatThrownBy(() -> service.getByCustomerNo("UNKNOWN"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("客户不存在: UNKNOWN");
    }

    @Test
    void listsActiveCustomers() {
        CustomerProfile first = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        CustomerProfile second = new CustomerProfile(
                "CUST002", "滨江市第一人民医院", 2, "事业单位", 201, "医院");
        CustomerProfileService service = new CustomerProfileService(new CustomerProfileRepository() {
            @Override
            public Optional<CustomerProfile> findByCustomerNo(String customerNo) {
                return Optional.empty();
            }

            @Override
            public List<CustomerProfile> findAllActive() {
                return List.of(first, second);
            }
        });

        assertThat(service.listActive()).containsExactly(first, second);
    }
}

