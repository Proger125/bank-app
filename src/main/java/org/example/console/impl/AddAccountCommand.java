package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.AccountDao;
import org.example.dao.BankDao;
import org.example.dao.ClientDao;
import org.example.dao.impl.AccountDaoImpl;
import org.example.dao.impl.BankDaoImpl;
import org.example.dao.impl.ClientDaoImpl;
import org.example.model.Account;
import org.example.model.Bank;
import org.example.model.Client;

import java.util.InputMismatchException;
import java.util.List;

import static org.example.util.CommandUtils.isUsdOrEur;

public class AddAccountCommand extends Command {
    private final BankDao bankDao = BankDaoImpl.getInstance();
    private final ClientDao clientDao = ClientDaoImpl.getInstance();
    private final AccountDao accountDao = AccountDaoImpl.getInstance();
    @Override
    public void execute() {
        final List<Bank> allBanks = bankDao.getAll();
        int bankId;
        while (true) {
            System.out.println("Choose one bank from the list:");
            allBanks.forEach(bank -> System.out.println(bank.getId() + ". " + bank.getName()));
            try {
                bankId = scanner.nextInt();
                final int finalBankId = bankId;
                if (allBanks.stream().anyMatch(bank -> bank.getId() == finalBankId)) {
                    break;
                }
                System.out.println("Bank id should be from the list");
            } catch (InputMismatchException e) {
                System.out.println("Bank id should be integer");
            }
        }

        final List<Client> allClients = clientDao.getALl();
        int clientId;
        while (true) {
            System.out.println("Choose one client from the list:");
            allClients.forEach(client -> System.out.println(client.getId() + ". " + client.getName()));
            try {
                clientId = scanner.nextInt();
                final int finalClientId = clientId;
                if (allClients.stream().anyMatch(client -> client.getId() == finalClientId)) {
                    break;
                }
                System.out.println("Client id should be from the list");
            } catch (InputMismatchException e) {
                System.out.println("Client id should be integer");
            }
        }

        String currency;
        while (true) {
            System.out.println("Choose currency: USD or EUR");
            currency = scanner.next();
            if (isUsdOrEur(currency)) {
                break;
            }
            System.out.println("Currency should be USD or EUR");
        }

        int rest;
        while (true) {
            System.out.println("Enter your bank account rest");
            try {
                rest = scanner.nextInt();
                if (rest > 0) {
                    break;
                }
                System.out.println("Your account rest should be > 0");
            } catch (InputMismatchException e) {
                System.out.println("Your account rest should be integer");
            }
        }
        final Account account = accountDao.create(new Account(bankId, clientId, currency, rest));
        System.out.println("New account was created: " + account.getId());
    }
}
