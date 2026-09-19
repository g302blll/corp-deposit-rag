package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.InterestMath;
import org.springframework.stereotype.Service;

@Service
public class InterestCalculationService {

    public long calculate(long principalInCents, long interestRate, int actualDays) {
        return InterestMath.simpleInterestInCents(principalInCents, interestRate, actualDays);
    }
}

