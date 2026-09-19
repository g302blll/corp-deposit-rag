package com.ljp.corpdeposit.core;

import java.util.List;

public record IntentionResult(
        String intentionNo,
        String customerNo,
        int status,
        List<String> detailNos) {
}

