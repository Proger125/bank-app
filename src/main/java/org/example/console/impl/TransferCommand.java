package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.AccountDao;
import org.example.dao.impl.AccountDaoImpl;
import org.example.exception.BankAppException;
import org.example.model.Account;
import org.example.service.TransferService;
import org.example.service.impl.TransferServiceImpl;

import java.util.InputMismatchException;
import java.util.List;

public class TransferCommand extends Command {
    private final TransferService transferService = new TransferServiceImpl();
    private final AccountDao accountDao = AccountDaoImpl.getInstance();
    @Override
    public void execute() {
        final List<Account> allAccounts = accountDao.getAll();
        int senderAccountId;
        while (true) {
            System.out.println("Choose sender account id from the list:");
            allAccounts.forEach(account -> System.out.println(account.getId() + ". " + account.getClientId() + " " + account.getRest() + " " + account.getCurrency()));
            try{
                senderAccountId = scanner.nextInt();
                final int finalSenderAccountId = senderAccountId;
                if (allAccounts.stream().anyMatch(account -> account.getId() == finalSenderAccountId)) {
                    break;
                }
                System.out.println("Account id should be from the list");
            } catch (InputMismatchException e) {
                System.out.println("Account id should be integer");
            }
        }

        int receiverAccountId;
        while (true) {
            System.out.println("Choose receiver account id from the list:");
            allAccounts.forEach(account -> System.out.println(account.getId() + ". " + account.getClientId() + " " + account.getRest() + " " + account.getCurrency()));
            try{
                receiverAccountId = scanner.nextInt();
                final int finalReceiverAccountId = receiverAccountId;
                if (allAccounts.stream().anyMatch(account -> account.getId() == finalReceiverAccountId) && receiverAccountId != senderAccountId) {
                    break;
                }
                System.out.println("Account id should be from the list and doesn't equal to sender account");
            } catch (InputMismatchException e) {
                System.out.println("Account id should be integer");
            }
        }

        int amount;
        while (true) {
            System.out.println("Enter your transfer amount");
            try {
                amount = scanner.nextInt();
                if (amount > 0) {
                    break;
                }
                System.out.println("Your transfer amount should be > 0");
            } catch (InputMismatchException e) {
                System.out.println("Your transfer amount should be integer");
            }
        }
        try {
            transferService.transfer(senderAccountId, receiverAccountId, amount);
        } catch (BankAppException e) {
            System.out.println(e.getMessage());
        }
    }
}
