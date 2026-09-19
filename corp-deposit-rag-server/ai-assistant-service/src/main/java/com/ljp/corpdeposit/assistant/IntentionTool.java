package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;

@FunctionalInterface
public interface IntentionTool {
    IntentionResult create(CreateIntentionCommand command);
}

