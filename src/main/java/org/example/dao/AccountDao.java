package org.example.dao;

import org.example.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountDao {
    Account create(final Account account);
    Optional<Account> getById(final int id);
    List<Account> getClientAccounts(final int clientId);
    List<Account> getAll();
    boolean isAccountExists(final int id);
    boolean isEnoughMoneyInAccount(final int id, final int requestedAmount);
}
