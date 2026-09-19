package com.ljp.corpdeposit.assistant;

import com.ljp.corpdeposit.core.CustomerProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpCustomerTool implements CustomerTool {
    private final RestClient client;
    public HttpCustomerTool(RestClient.Builder builder, @Value("${services.customer-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }
    @Override public CustomerProfile getCustomerProfile(String customerNo) {
        return client.get().uri("/api/v1/customers/{customerNo}", customerNo).retrieve().body(CustomerProfile.class);
    }
}

