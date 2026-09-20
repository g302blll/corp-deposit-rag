package com.ljp.corpdeposit.business;

import java.time.LocalDateTime;

public record IntentionSummary(
        String intentionNo,
        String customerNo,
        String requirementText,
        Long totalAmountInCents,
        Integer status,
        Integer sourceChannel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
