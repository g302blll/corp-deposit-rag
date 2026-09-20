package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

    @Test
    void bindsCustomerNumberFromPath() throws Exception {
        CustomerProfile profile = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        CustomerProfileService service = new CustomerProfileService(new CustomerProfileRepository() {
            @Override
            public Optional<CustomerProfile> findByCustomerNo(String customerNo) {
                return Optional.of(profile);
            }

            @Override
            public List<CustomerProfile> findAllActive() {
                return List.of(profile);
            }
        });
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service)).build();

        mvc.perform(get("/api/v1/customers/CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNo").value("CUST001"));
    }

    @Test
    void listsActiveCustomers() throws Exception {
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
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service)).build();

        mvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerNo").value("CUST001"))
                .andExpect(jsonPath("$[1].customerNo").value("CUST002"));
    }

    @Test
    void serializesEmptyCustomerListAsArray() throws Exception {
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
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service)).build();

        mvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void returnsCustomerNotFoundErrorCode() throws Exception {
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
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(get("/api/v1/customers/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }
}

