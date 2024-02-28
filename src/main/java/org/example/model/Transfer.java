package org.example.model;

import java.util.Date;

public class Transfer {
    private int id;
    private int senderAccountId;
    private int receiverAccountId;
    private int amount;
    private Date date;

    public Transfer(final int id, final int senderAccountId, final int receiverAccountId, final int amount, final Date date) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.date = date;
    }

    public Transfer(final int senderAccountId, final int receiverAccountId, final int amount) {
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
    }

    public Transfer() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(final int senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(final int receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }
}
