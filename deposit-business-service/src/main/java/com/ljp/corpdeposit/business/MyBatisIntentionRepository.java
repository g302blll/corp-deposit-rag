package com.ljp.corpdeposit.business;

import com.ljp.corpdeposit.core.CreateIntentionCommand;
import com.ljp.corpdeposit.core.IntentionDetailCommand;
import com.ljp.corpdeposit.core.IntentionResult;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MyBatisIntentionRepository implements IntentionRepository {

    private static final DateTimeFormatter NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final IntentionMapper mapper;

    public MyBatisIntentionRepository(IntentionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<IntentionResult> findByIdempotencyKey(String key) {
        IntentionMasterRow master = mapper.findByIdempotencyKey(key);
        if (master == null) {
            return Optional.empty();
        }
        return Optional.of(new IntentionResult(
                master.getIntentionNo(), master.getCustomerNo(), master.getStatus(),
                mapper.findDetailNos(master.getSnId())));
    }

    @Override
    public IntentionResult create(CreateIntentionCommand command) {
        IntentionMasterRow master = new IntentionMasterRow();
        master.setIntentionNo(businessNo("INT"));
        master.setIdempotencyKey(command.idempotencyKey());
        master.setCustomerNo(command.customerNo());
        master.setRequirementText(command.requirementText());
        master.setTotalAmount(command.details().stream()
                .mapToLong(IntentionDetailCommand::amountInCents)
                .reduce(0L, Math::addExact));
        if (mapper.insertMaster(master) == 0) {
            return findByIdempotencyKey(command.idempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("幂等意向创建冲突"));
        }

        List<String> detailNos = new ArrayList<>();
        for (IntentionDetailCommand commandDetail : command.details()) {
            String detailNo = businessNo("DET");
            IntentionDetailRow detail = new IntentionDetailRow();
            detail.setIntentionId(master.getSnId());
            detail.setDetailNo(detailNo);
            detail.setProductId(commandDetail.productId());
            detail.setProductTermId(commandDetail.productTermId());
            detail.setAmount(commandDetail.amountInCents());
            detail.setInterestRate(commandDetail.interestRate());
            detail.setExpectedInterest(commandDetail.expectedInterestInCents());
            detail.setCurrencyCode(commandDetail.currencyCode());
            mapper.insertDetail(detail);
            detailNos.add(detailNo);
        }
        return new IntentionResult(master.getIntentionNo(), command.customerNo(), 1, List.copyOf(detailNos));
    }

    private String businessNo(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + LocalDateTime.now().format(NUMBER_TIME) + suffix;
    }
}

