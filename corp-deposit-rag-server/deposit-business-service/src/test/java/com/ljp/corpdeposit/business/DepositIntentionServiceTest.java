package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionDetailCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepositIntentionServiceTest {

    @Test
    void listsIntentionsWithCustomerAndStatusFilters() {
        IntentionSummary expected = new IntentionSummary(
                "INT001", "CUST001", "800万存一年", 800_000_000L, 1, 1,
                LocalDateTime.of(2026, 9, 20, 9, 0), LocalDateTime.of(2026, 9, 20, 10, 0));
        RecordingRepository repository = new RecordingRepository();
        repository.summaries = List.of(expected);

        List<IntentionSummary> actual = new DepositIntentionService(repository).list("CUST001", 1);

        assertThat(actual).containsExactly(expected);
        assertThat(repository.customerNo).isEqualTo("CUST001");
        assertThat(repository.status).isEqualTo(1);
    }

    @Test
    void normalizesCustomerFilterBeforeQueryingRepository() {
        RecordingRepository repository = new RecordingRepository();
        DepositIntentionService service = new DepositIntentionService(repository);

        service.list(" CUST001 ", 1);
        assertThat(repository.customerNo).isEqualTo("CUST001");

        service.list("   ", 1);
        assertThat(repository.customerNo).isNull();
    }

    @Test
    void getsIntentionWithDetails() {
        IntentionLineView line = new IntentionLineView(
                "DET001", 2L, 23L, "001", 800_000_000L, 15_000L,
                12_000_000L, 0, LocalDateTime.of(2026, 9, 20, 9, 1),
                LocalDateTime.of(2026, 9, 20, 9, 1));
        IntentionDetailView expected = new IntentionDetailView(
                "INT001", "CUST001", "800万存一年", 800_000_000L, 1, 1,
                LocalDateTime.of(2026, 9, 20, 9, 0), LocalDateTime.of(2026, 9, 20, 10, 0),
                List.of(line));
        RecordingRepository repository = new RecordingRepository();
        repository.detail = Optional.of(expected);

        IntentionDetailView actual = new DepositIntentionService(repository).get("INT001");

        assertThat(actual).isEqualTo(expected);
        assertThat(repository.intentionNo).isEqualTo("INT001");
    }

    @Test
    void rejectsUnknownIntention() {
        DepositIntentionService service = new DepositIntentionService(new RecordingRepository());

        assertThatThrownBy(() -> service.get("UNKNOWN"))
                .isInstanceOfSatisfying(com.ljp.corpdeposit.web.BusinessException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("INTENTION_NOT_FOUND");
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("办理意向不存在: UNKNOWN");
                });
    }

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

            @Override
            public List<IntentionSummary> findAll(String customerNo, Integer status) {
                return List.of();
            }

            @Override
            public Optional<IntentionDetailView> findByIntentionNo(String intentionNo) {
                return Optional.empty();
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

    private static final class RecordingRepository implements IntentionRepository {
        private List<IntentionSummary> summaries = List.of();
        private Optional<IntentionDetailView> detail = Optional.empty();
        private String customerNo;
        private Integer status;
        private String intentionNo;

        @Override
        public Optional<IntentionResult> findByIdempotencyKey(String key) {
            return Optional.empty();
        }

        @Override
        public IntentionResult create(CreateIntentionCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<IntentionSummary> findAll(String customerNo, Integer status) {
            this.customerNo = customerNo;
            this.status = status;
            return summaries;
        }

        @Override
        public Optional<IntentionDetailView> findByIntentionNo(String intentionNo) {
            this.intentionNo = intentionNo;
            return detail;
        }
    }
}

