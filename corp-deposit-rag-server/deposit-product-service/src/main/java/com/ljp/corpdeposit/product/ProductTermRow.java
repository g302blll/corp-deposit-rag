package com.ljp.corpdeposit.product;

public class ProductTermRow {
    private Long productId;
    private String productCode;
    private String productName;
    private Integer depositType;
    private Long productTermId;
    private String termCode;
    private String termName;
    private Integer termDays;
    private Long minOpenAmount;
    private Long minRetainAmount;
    private Integer noticeDays;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getDepositType() { return depositType; }
    public void setDepositType(Integer depositType) { this.depositType = depositType; }
    public Long getProductTermId() { return productTermId; }
    public void setProductTermId(Long productTermId) { this.productTermId = productTermId; }
    public String getTermCode() { return termCode; }
    public void setTermCode(String termCode) { this.termCode = termCode; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
    public Integer getTermDays() { return termDays; }
    public void setTermDays(Integer termDays) { this.termDays = termDays; }
    public Long getMinOpenAmount() { return minOpenAmount; }
    public void setMinOpenAmount(Long minOpenAmount) { this.minOpenAmount = minOpenAmount; }
    public Long getMinRetainAmount() { return minRetainAmount; }
    public void setMinRetainAmount(Long minRetainAmount) { this.minRetainAmount = minRetainAmount; }
    public Integer getNoticeDays() { return noticeDays; }
    public void setNoticeDays(Integer noticeDays) { this.noticeDays = noticeDays; }
}

