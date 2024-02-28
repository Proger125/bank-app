package org.example.service;

import org.example.exception.BankAppException;
import org.example.model.Transfer;

public interface TransferService {
    Transfer transfer(final int senderAccountId, final int receiverAccountId, final int amount) throws BankAppException;
}
