package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;

import java.util.List;
import java.util.Optional;

public interface IntentionRepository {
    Optional<IntentionResult> findByIdempotencyKey(String key);
    IntentionResult create(CreateIntentionCommand command);
    List<IntentionSummary> findAll(String customerNo, Integer status);
    Optional<IntentionDetailView> findByIntentionNo(String intentionNo);
}

