package com.ljp.corpdeposit.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import com.ljp.corpdeposit.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepositIntentionControllerTest {

    @Test
    void listsWithoutFilters() throws Exception {
        RecordingRepository repository = repository();
        MockMvc mvc = mvc(repository);

        mvc.perform(get("/api/v1/intentions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].intentionNo").value("INT001"));

        assertThat(repository.customerNo).isNull();
        assertThat(repository.status).isNull();
    }

    @Test
    void listsWithBothFilters() throws Exception {
        RecordingRepository repository = repository();
        MockMvc mvc = mvc(repository);

        mvc.perform(get("/api/v1/intentions")
                        .param("customerNo", "CUST001")
                        .param("status", "1"))
                .andExpect(status().isOk());

        assertThat(repository.customerNo).isEqualTo("CUST001");
        assertThat(repository.status).isEqualTo(1);
    }

    @Test
    void returnsDetailJson() throws Exception {
        MockMvc mvc = mvc(repository());

        mvc.perform(get("/api/v1/intentions/INT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentionNo").value("INT001"))
                .andExpect(jsonPath("$.totalAmountInCents").value(800_000_000L))
                .andExpect(jsonPath("$.details[0].detailNo").value("DET001"))
                .andExpect(jsonPath("$.details[0].amountInCents").value(800_000_000L));
    }

    @Test
    void returnsBusinessErrorForUnknownIntention() throws Exception {
        MockMvc mvc = mvc(repository());

        mvc.perform(get("/api/v1/intentions/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTENTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("办理意向不存在: UNKNOWN"));
    }

    @Test
    void keepsPostRouteAvailable() throws Exception {
        RecordingRepository repository = repository();
        MockMvc mvc = mvc(repository);
        String request = new ObjectMapper().writeValueAsString(new CreateIntentionCommand(
                "idem-001", "CUST001", "800万存一年",
                List.of(new com.ljp.corpdeposit.core.IntentionDetailCommand(
                        2L, 23L, 800_000_000L, 15_000L, 12_000_000L, "001"))));

        mvc.perform(post("/api/v1/intentions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentionNo").value("INT001"));
    }

    private MockMvc mvc(RecordingRepository repository) {
        DepositIntentionService service = new DepositIntentionService(repository);
        return MockMvcBuilders.standaloneSetup(new DepositIntentionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private RecordingRepository repository() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 9, 20, 9, 0);
        IntentionLineView line = new IntentionLineView(
                "DET001", 2L, 23L, "001", 800_000_000L, 15_000L,
                12_000_000L, 0, timestamp, timestamp);
        IntentionDetailView detail = new IntentionDetailView(
                "INT001", "CUST001", "800万存一年", 800_000_000L, 1, 1,
                timestamp, timestamp, List.of(line));
        return new RecordingRepository(detail);
    }

    private static final class RecordingRepository implements IntentionRepository {
        private final IntentionDetailView detail;
        private String customerNo;
        private Integer status;

        private RecordingRepository(IntentionDetailView detail) {
            this.detail = detail;
        }

        @Override
        public Optional<IntentionResult> findByIdempotencyKey(String key) {
            return Optional.empty();
        }

        @Override
        public IntentionResult create(CreateIntentionCommand command) {
            return new IntentionResult("INT001", command.customerNo(), 1, List.of("DET001"));
        }

        @Override
        public List<IntentionSummary> findAll(String customerNo, Integer status) {
            this.customerNo = customerNo;
            this.status = status;
            return List.of(new IntentionSummary(
                    detail.intentionNo(), detail.customerNo(), detail.requirementText(),
                    detail.totalAmountInCents(), detail.status(), detail.sourceChannel(),
                    detail.createdAt(), detail.updatedAt()));
        }

        @Override
        public Optional<IntentionDetailView> findByIntentionNo(String intentionNo) {
            return intentionNo.equals(detail.intentionNo()) ? Optional.of(detail) : Optional.empty();
        }
    }
}
