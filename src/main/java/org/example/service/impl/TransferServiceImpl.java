package org.example.service.impl;

import org.example.dao.AccountDao;
import org.example.dao.TransferDao;
import org.example.dao.impl.AccountDaoImpl;
import org.example.dao.impl.TransferDaoImpl;
import org.example.exception.BankAppException;
import org.example.model.Transfer;
import org.example.service.TransferService;

public class TransferServiceImpl implements TransferService {

    private final AccountDao accountDao = AccountDaoImpl.getInstance();
    private final TransferDao transferDao = TransferDaoImpl.getInstance();
    @Override
    public Transfer transfer(final int senderAccountId, final int receiverAccountId, final int amount) throws BankAppException {
        if (accountDao.isAccountExists(senderAccountId) && accountDao.isAccountExists(receiverAccountId)) {
            if (accountDao.isEnoughMoneyInAccount(senderAccountId, amount)) {
                return transferDao.create(new Transfer(senderAccountId, receiverAccountId, amount));
            } else {
                throw new BankAppException("Sender account doesn't have enough money");
            }
        } else {
            throw new BankAppException("Sender or receiver account doesn't exist");
        }
    }
}
