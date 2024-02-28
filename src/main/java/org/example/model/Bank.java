package org.example.model;

public class Bank {
    private int id;
    private String name;
    private int individualCommission;
    private int legalCommission;

    public Bank() {}
    public Bank(final String name, final int individualCommission, final int legalCommission) {
        this.name = name;
        this.individualCommission = individualCommission;
        this.legalCommission = legalCommission;
    }

    public Bank(final int id, final String name, final int individualCommission, final int legalCommission) {
        this.id = id;
        this.name = name;
        this.individualCommission = individualCommission;
        this.legalCommission = legalCommission;
    }

    public int getLegalCommission() {
        return legalCommission;
    }

    public void setLegalCommission(final int legalCommission) {
        this.legalCommission = legalCommission;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getIndividualCommission() {
        return individualCommission;
    }

    public void setIndividualCommission(final int individualCommission) {
        this.individualCommission = individualCommission;
    }
}
