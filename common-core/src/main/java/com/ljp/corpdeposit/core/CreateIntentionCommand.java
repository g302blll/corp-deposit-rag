package com.ljp.corpdeposit.core;

import java.util.List;

public record CreateIntentionCommand(
        String idempotencyKey,
        String customerNo,
        String requirementText,
        List<IntentionDetailCommand> details) {
}

