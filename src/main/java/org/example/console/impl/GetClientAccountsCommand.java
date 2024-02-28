package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.AccountDao;
import org.example.dao.ClientDao;
import org.example.dao.impl.AccountDaoImpl;
import org.example.dao.impl.ClientDaoImpl;
import org.example.model.Account;
import org.example.model.Client;

import java.util.InputMismatchException;
import java.util.List;

public class GetClientAccountsCommand extends Command {
    private final ClientDao clientDao = ClientDaoImpl.getInstance();
    private final AccountDao accountDao = AccountDaoImpl.getInstance();
    @Override
    public void execute() {
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

        final List<Account> accounts = accountDao.getClientAccounts(clientId);
        System.out.println("All accounts for client: " + clientId);
        accounts.forEach(account -> System.out.println("Id: " + account.getId() + "Client id: " + account.getClientId() + " Bank id: " + account.getBankId() +
                " Rest: " + account.getRest() + " Currency: " + account.getCurrency()));
    }
}
