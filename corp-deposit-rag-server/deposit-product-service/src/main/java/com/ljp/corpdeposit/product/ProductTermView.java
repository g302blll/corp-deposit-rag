package com.ljp.corpdeposit.product;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ProductTermView(
        @JsonSerialize(using = ToStringSerializer.class) Long productTermId,
        String termCode,
        String termName,
        Integer termDays,
        Long minOpenAmountInCents,
        Long minRetainAmountInCents,
        Integer noticeDays) {
}
