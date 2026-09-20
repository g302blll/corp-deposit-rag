package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerProfileService customerProfileService;

    public CustomerController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @GetMapping
    public List<CustomerProfile> listCustomers() {
        return customerProfileService.listActive();
    }

    @GetMapping("/{customerNo}")
    public CustomerProfile getCustomer(@PathVariable String customerNo) {
        return customerProfileService.getByCustomerNo(customerNo);
    }
}

