package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.DepositRequirement;
import com.ljp.corpdeposit.core.LiquidityPreference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockRequirementExtractorTest {

    private final MockRequirementExtractor extractor = new MockRequirementExtractor();

    @Test
    void extractsChineseAmountTermAndLiquidityWithoutCallingApi() {
        DepositRequirement result = extractor.extract("有800万资金想存一年，优先收益，可以长期不用");

        assertThat(result.amountInCents()).isEqualTo(800_000_000L);
        assertThat(result.preferredDays()).isEqualTo(365);
        assertThat(result.liquidityPreference()).isEqualTo(LiquidityPreference.LOW);
        assertThat(result.currencyCode()).isEqualTo("001");
    }

    @Test
    void recognizesHighLiquidityLanguage() {
        DepositRequirement result = extractor.extract("50万元，需要随时灵活支取");

        assertThat(result.amountInCents()).isEqualTo(50_000_000L);
        assertThat(result.preferredDays()).isNull();
        assertThat(result.liquidityPreference()).isEqualTo(LiquidityPreference.HIGH);
    }
}

