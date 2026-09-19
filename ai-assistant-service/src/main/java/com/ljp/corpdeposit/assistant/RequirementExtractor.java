package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.DepositRequirement;

@FunctionalInterface
public interface RequirementExtractor {
    DepositRequirement extract(String text);
}

