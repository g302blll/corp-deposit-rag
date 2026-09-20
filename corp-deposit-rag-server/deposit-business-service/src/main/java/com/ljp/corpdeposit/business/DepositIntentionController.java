package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping
    public List<IntentionSummary> list(
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) Integer status) {
        return service.list(customerNo, status);
    }

    @GetMapping("/{intentionNo}")
    public IntentionDetailView detail(@PathVariable String intentionNo) {
        return service.get(intentionNo);
    }
}

