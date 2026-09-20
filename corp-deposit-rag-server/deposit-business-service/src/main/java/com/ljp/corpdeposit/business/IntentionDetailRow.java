package com.ljp.corpdeposit.business;

import java.time.LocalDateTime;

public class IntentionDetailRow {
    private Long intentionId;
    private String detailNo;
    private Long productId;
    private Long productTermId;
    private Long amount;
    private Long interestRate;
    private Long expectedInterest;
    private String currencyCode;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getIntentionId() { return intentionId; }
    public void setIntentionId(Long intentionId) { this.intentionId = intentionId; }
    public String getDetailNo() { return detailNo; }
    public void setDetailNo(String detailNo) { this.detailNo = detailNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getProductTermId() { return productTermId; }
    public void setProductTermId(Long productTermId) { this.productTermId = productTermId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public Long getInterestRate() { return interestRate; }
    public void setInterestRate(Long interestRate) { this.interestRate = interestRate; }
    public Long getExpectedInterest() { return expectedInterest; }
    public void setExpectedInterest(Long expectedInterest) { this.expectedInterest = expectedInterest; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

