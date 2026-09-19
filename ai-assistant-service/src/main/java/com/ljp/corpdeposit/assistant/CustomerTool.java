package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CustomerProfile;

@FunctionalInterface
public interface CustomerTool {
    CustomerProfile getCustomerProfile(String customerNo);
}

