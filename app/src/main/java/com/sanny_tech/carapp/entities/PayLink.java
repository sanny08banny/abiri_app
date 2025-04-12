package com.sanny_tech.carapp.entities;

public class PayLink {
    private Long id;
    private String recipientNumber;
    private String shortcode;
    private String paybillNumber;
    private String accountNumber;
    private String pochiRecipientNumber;
    private String dateTimeCreated;

    public PayLink() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getPaybillNumber() {
        return paybillNumber;
    }

    public void setPaybillNumber(String paybillNumber) {
        this.paybillNumber = paybillNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getPochiRecipientNumber() {
        return pochiRecipientNumber;
    }

    public void setPochiRecipientNumber(String pochiRecipientNumber) {
        this.pochiRecipientNumber = pochiRecipientNumber;
    }

    public String getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(String dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }
}
