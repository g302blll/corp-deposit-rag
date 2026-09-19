package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;

import java.util.Optional;

@FunctionalInterface
public interface CustomerProfileRepository {

    Optional<CustomerProfile> findByCustomerNo(String customerNo);
}

