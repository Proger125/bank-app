package org.example.model;

public class Account {
    private int id;
    private int bankId;
    private int clientId;
    private String currency;
    private int rest;

    public Account(final int id, final int bankId, final int clientId, final String currency, final int rest) {
        this.id = id;
        this.bankId = bankId;
        this.clientId = clientId;
        this.currency = currency;
        this.rest = rest;
    }

    public Account(final int bankId, final String currency, final int rest) {
        this.bankId = bankId;
        this.currency = currency;
        this.rest = rest;
    }

    public Account(final int bankId, final int clientId, final String currency, final int rest) {
        this.bankId = bankId;
        this.clientId = clientId;
        this.currency = currency;
        this.rest = rest;
    }

    public int getId() {
        return id;
    }

    public Account() {
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(final int bankId) {
        this.bankId = bankId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(final int clientId) {
        this.clientId = clientId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public int getRest() {
        return rest;
    }

    public void setRest(final int rest) {
        this.rest = rest;
    }
}
