package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpProductTool implements ProductTool {
    private final RestClient client;
    public HttpProductTool(RestClient.Builder builder, @Value("${services.product-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }
    @Override public List<ProductPlan> match(ProductMatchRequest request) {
        List<ProductPlan> result = client.post().uri("/api/v1/products/matches").body(request).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return result == null ? List.of() : result;
    }
}

