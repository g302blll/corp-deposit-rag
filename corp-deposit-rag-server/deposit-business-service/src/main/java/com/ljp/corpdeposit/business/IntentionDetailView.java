package com.ljp.corpdeposit.business;

import java.time.LocalDateTime;
import java.util.List;

public record IntentionDetailView(
        String intentionNo,
        String customerNo,
        String requirementText,
        Long totalAmountInCents,
        Integer status,
        Integer sourceChannel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<IntentionLineView> details) {
}
