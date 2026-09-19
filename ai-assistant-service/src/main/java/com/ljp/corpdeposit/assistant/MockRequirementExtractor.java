package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.DepositRequirement;
import com.ljp.corpdeposit.core.LiquidityPreference;
import com.ljp.corpdeposit.web.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "assistant.extractor", havingValue = "mock", matchIfMissing = true)
public class MockRequirementExtractor implements RequirementExtractor {

    private static final Pattern AMOUNT = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(亿元|亿|万元|万|元)?");

    @Override
    public DepositRequirement extract(String text) {
        Matcher matcher = AMOUNT.matcher(text == null ? "" : text);
        if (!matcher.find()) {
            throw new BusinessException("REQUIREMENT_AMOUNT_MISSING", "未识别到存款金额", HttpStatus.BAD_REQUEST);
        }
        BigDecimal yuan = new BigDecimal(matcher.group(1)).multiply(multiplier(matcher.group(2)));
        long cents = yuan.multiply(BigDecimal.valueOf(100L)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new DepositRequirement(cents, preferredDays(text), liquidity(text), "001");
    }

    private BigDecimal multiplier(String unit) {
        if (unit == null || "元".equals(unit)) return BigDecimal.ONE;
        if (unit.contains("亿")) return BigDecimal.valueOf(100_000_000L);
        return BigDecimal.valueOf(10_000L);
    }

    private Integer preferredDays(String text) {
        if (text == null) return null;
        if (text.matches(".*(?:一年|1年).*")) return 365;
        if (text.matches(".*(?:半年|六个月|6个月).*")) return 180;
        if (text.matches(".*(?:三个月|3个月).*")) return 90;
        if (text.matches(".*(?:七天|7天).*")) return 7;
        if (text.matches(".*(?:一天|1天).*")) return 1;
        return null;
    }

    private LiquidityPreference liquidity(String text) {
        if (text != null && text.matches(".*(?:随时|灵活|流动|支取).*")) return LiquidityPreference.HIGH;
        if (text != null && text.matches(".*(?:收益|长期|不用).*")) return LiquidityPreference.LOW;
        return LiquidityPreference.MEDIUM;
    }
}

