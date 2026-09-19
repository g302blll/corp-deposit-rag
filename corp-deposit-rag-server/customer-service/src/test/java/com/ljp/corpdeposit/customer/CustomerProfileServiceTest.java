package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.web.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerProfileServiceTest {

    @Test
    void returnsCustomerWithLargeAndSmallCategory() {
        CustomerProfile expected = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        CustomerProfileService service = new CustomerProfileService(
                customerNo -> Optional.of(expected));

        CustomerProfile actual = service.getByCustomerNo("CUST001");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void rejectsUnknownCustomerNumber() {
        CustomerProfileService service = new CustomerProfileService(customerNo -> Optional.empty());

        assertThatThrownBy(() -> service.getByCustomerNo("UNKNOWN"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("客户不存在: UNKNOWN");
    }
}

