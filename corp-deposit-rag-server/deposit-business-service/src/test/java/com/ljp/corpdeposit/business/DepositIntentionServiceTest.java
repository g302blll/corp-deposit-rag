package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionDetailCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DepositIntentionServiceTest {

    @Test
    void createsOneMasterWithMultipleDetailsAndReusesIdempotentResult() {
        AtomicInteger creates = new AtomicInteger();
        IntentionResult stored = new IntentionResult(
                "INT202609190001", "CUST001", 1, List.of("DET001", "DET002"));
        IntentionRepository repository = new IntentionRepository() {
            private boolean created;

            @Override
            public Optional<IntentionResult> findByIdempotencyKey(String key) {
                return created ? Optional.of(stored) : Optional.empty();
            }

            @Override
            public IntentionResult create(CreateIntentionCommand command) {
                creates.incrementAndGet();
                created = true;
                return stored;
            }
        };
        DepositIntentionService service = new DepositIntentionService(repository);
        CreateIntentionCommand command = new CreateIntentionCommand(
                "idem-001", "CUST001", "800万存一年",
                List.of(
                        new IntentionDetailCommand(2L, 23L, 500_000_000L, 15_000L, 7_500_000L, "001"),
                        new IntentionDetailCommand(3L, 32L, 300_000_000L, 10_000L, 57_534L, "001")));

        IntentionResult first = service.create(command);
        IntentionResult retry = service.create(command);

        assertThat(first).isEqualTo(stored);
        assertThat(retry).isEqualTo(stored);
        assertThat(first.detailNos()).hasSize(2);
        assertThat(creates).hasValue(1);
    }
}

