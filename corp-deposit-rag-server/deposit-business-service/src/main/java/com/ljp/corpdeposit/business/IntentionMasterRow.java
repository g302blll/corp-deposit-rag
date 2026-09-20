package com.ljp.corpdeposit.business;

import java.time.LocalDateTime;

public class IntentionMasterRow {
    private Long snId;
    private String intentionNo;
    private String customerNo;
    private Integer status;
    private String idempotencyKey;
    private String requirementText;
    private Long totalAmount;
    private Integer sourceChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getSnId() { return snId; }
    public void setSnId(Long snId) { this.snId = snId; }
    public String getIntentionNo() { return intentionNo; }
    public void setIntentionNo(String intentionNo) { this.intentionNo = intentionNo; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public Integer getSourceChannel() { return sourceChannel; }
    public void setSourceChannel(Integer sourceChannel) { this.sourceChannel = sourceChannel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

