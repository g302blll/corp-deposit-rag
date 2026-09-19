package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

    @Test
    void bindsCustomerNumberFromPath() throws Exception {
        CustomerProfile profile = new CustomerProfile(
                "CUST001", "华星科技有限公司", 4, "民营企业", 401, "科技型民营企业");
        CustomerProfileService service = new CustomerProfileService(number -> Optional.of(profile));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service)).build();

        mvc.perform(get("/api/v1/customers/CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNo").value("CUST001"));
    }
}

