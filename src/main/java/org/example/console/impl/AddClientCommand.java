package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.BankDao;
import org.example.dao.ClientDao;
import org.example.dao.impl.BankDaoImpl;
import org.example.dao.impl.ClientDaoImpl;
import org.example.model.Account;
import org.example.model.Bank;
import org.example.model.Client;
import org.example.model.ClientType;

import java.util.InputMismatchException;
import java.util.List;

import static org.example.util.CommandUtils.isUsdOrEur;

public class AddClientCommand extends Command {
    private final BankDao bankDao = BankDaoImpl.getInstance();
    private final ClientDao clientDao = ClientDaoImpl.getInstance();
    @Override
    public void execute() {
        System.out.println("Enter client name:");
        final String clientName = scanner.next();

        String clientType;
        while (true) {
            System.out.println("Choose client type: INDIVIDUAL or LEGAL");
            try {
                clientType = scanner.next();
                if ("INDIVIDUAL".equals(clientType) || "LEGAL".equals(clientType)) {
                    break;
                }
                System.out.println("Client type could be 1 or 2");
            } catch (InputMismatchException e) {
                System.out.println("Client type should be integer");
            }
        }
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
        final Client client = clientDao.create(new Client(clientName, ClientType.valueOf(clientType), List.of(new Account(bankId, currency, rest))));
        System.out.println("New client was created: " + client.getId());
    }
}
