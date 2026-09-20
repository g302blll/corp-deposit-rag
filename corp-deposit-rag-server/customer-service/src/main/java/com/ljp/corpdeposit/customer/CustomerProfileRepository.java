package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;

import java.util.List;
import java.util.Optional;

public interface CustomerProfileRepository {

    Optional<CustomerProfile> findByCustomerNo(String customerNo);

    List<CustomerProfile> findAllActive();
}

