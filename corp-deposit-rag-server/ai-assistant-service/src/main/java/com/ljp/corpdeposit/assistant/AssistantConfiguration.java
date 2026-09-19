package com.ljp.corpdeposit.assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AssistantConfiguration {
    @Bean
    Clock systemClock() { return Clock.systemDefaultZone(); }
}

