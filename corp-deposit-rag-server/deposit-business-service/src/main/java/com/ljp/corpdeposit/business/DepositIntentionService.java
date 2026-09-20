package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import com.ljp.corpdeposit.web.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepositIntentionService {

    private final IntentionRepository repository;

    public DepositIntentionService(IntentionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IntentionResult create(CreateIntentionCommand command) {
        validate(command);
        return repository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> repository.create(command));
    }

    @Transactional(readOnly = true)
    public List<IntentionSummary> list(String customerNo, Integer status) {
        String normalizedCustomerNo = customerNo == null ? null : customerNo.strip();
        if (normalizedCustomerNo != null && normalizedCustomerNo.isEmpty()) {
            normalizedCustomerNo = null;
        }
        return repository.findAll(normalizedCustomerNo, status);
    }

    @Transactional(readOnly = true)
    public IntentionDetailView get(String intentionNo) {
        return repository.findByIntentionNo(intentionNo)
                .orElseThrow(() -> new BusinessException(
                        "INTENTION_NOT_FOUND", "办理意向不存在: " + intentionNo, HttpStatus.NOT_FOUND));
    }

    private void validate(CreateIntentionCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new BusinessException("INVALID_REQUEST", "幂等键不能为空", HttpStatus.BAD_REQUEST);
        }
        if (command.details() == null || command.details().isEmpty()) {
            throw new BusinessException("INVALID_REQUEST", "办理意向至少包含一条明细", HttpStatus.BAD_REQUEST);
        }
    }
}

