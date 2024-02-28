package org.example.dao;

import org.example.model.Bank;

import java.util.List;
import java.util.Optional;

public interface BankDao {
    Bank create(final Bank bank);
    Optional<Bank> getById(final int id);
    List<Bank> getAll();
    Bank update(final Bank bank);
    void deleteById(final int id);
}
