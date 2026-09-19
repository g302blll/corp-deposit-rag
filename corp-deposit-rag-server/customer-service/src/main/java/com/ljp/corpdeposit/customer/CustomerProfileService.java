package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import com.ljp.corpdeposit.web.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository repository;

    public CustomerProfileService(CustomerProfileRepository repository) {
        this.repository = repository;
    }

    public CustomerProfile getByCustomerNo(String customerNo) {
        return repository.findByCustomerNo(customerNo)
                .orElseThrow(() -> new BusinessException(
                        "CUSTOMER_NOT_FOUND", "客户不存在: " + customerNo, HttpStatus.NOT_FOUND));
    }
}

