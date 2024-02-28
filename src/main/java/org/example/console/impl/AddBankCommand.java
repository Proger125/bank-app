package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.BankDao;
import org.example.dao.impl.BankDaoImpl;
import org.example.model.Bank;

import java.util.InputMismatchException;

public class AddBankCommand extends Command {
    private BankDao bankDao = BankDaoImpl.getInstance();
    @Override
    public void execute() {
        System.out.println("Enter bank name:");
        final String bankName = scanner.next();
        int individualCommission;
        while (true) {
            System.out.println("Enter individual commission:");
            try {
                individualCommission = scanner.nextInt();
                if (isCommissionValid(individualCommission)) {
                    break;
                }
                System.out.println("Commission should be >= 0 and < 100");
            } catch (InputMismatchException e) {
                System.out.println("Individual commission should be integer");
            }
        }

        int legalCommission;
        while (true) {
            System.out.println("Enter legal commission:");
            try {
                legalCommission = scanner.nextInt();
                if (isCommissionValid(legalCommission)) {
                    break;
                }
                System.out.println("Commission should be >= 0 and < 100");
            } catch (InputMismatchException e) {
                System.out.println("Legal commission should be integer");
            }
        }

        final Bank bank = bankDao.create(new Bank(bankName, individualCommission, legalCommission));
        System.out.println("New bank was created: " + bank.getId());
    }

    private boolean isCommissionValid(final int commission) {
        return commission >= 0 && commission < 100;
    }
}
