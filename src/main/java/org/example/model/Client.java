package org.example.model;

import java.util.List;

public class Client {
    private int id;
    private String name;
    private ClientType clientType;
    private List<Account> accounts;

    public Client(final int id, final String name, final ClientType clientType, final List<Account> accounts) {
        this.id = id;
        this.name = name;
        this.clientType = clientType;
        this.accounts = accounts;
    }

    public Client(final String name, final ClientType clientType, final List<Account> accounts) {
        this.name = name;
        this.clientType = clientType;
        this.accounts = accounts;
    }

    public Client() {}

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(final List<Account> accounts) {
        this.accounts = accounts;
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

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(final ClientType clientType) {
        this.clientType = clientType;
    }
}
