package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AiAssistantController {
    private final AiOrchestratorService orchestrator;
    private final IntentionTool intentionTool;
    public AiAssistantController(AiOrchestratorService orchestrator, IntentionTool intentionTool) {
        this.orchestrator = orchestrator;
        this.intentionTool = intentionTool;
    }
    @PostMapping("/plans") public RecommendationResponse plans(@RequestBody RecommendationRequest request) {
        return orchestrator.recommend(request.customerNo(), request.message());
    }
    @PostMapping("/intentions") public IntentionResult intention(@RequestBody CreateIntentionCommand command) {
        return intentionTool.create(command);
    }
}

