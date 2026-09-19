package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/intentions")
public class DepositIntentionController {

    private final DepositIntentionService service;

    public DepositIntentionController(DepositIntentionService service) {
        this.service = service;
    }

    @PostMapping
    public IntentionResult create(@RequestBody CreateIntentionCommand command) {
        return service.create(command);
    }
}

