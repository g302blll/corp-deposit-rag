package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpIntentionTool implements IntentionTool {
    private final RestClient client;
    public HttpIntentionTool(RestClient.Builder builder, @Value("${services.business-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }
    @Override public IntentionResult create(CreateIntentionCommand command) {
        return client.post().uri("/api/v1/intentions").body(command).retrieve().body(IntentionResult.class);
    }
}

