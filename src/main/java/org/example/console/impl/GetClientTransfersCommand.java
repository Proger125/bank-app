package org.example.console.impl;

import org.example.console.Command;
import org.example.dao.ClientDao;
import org.example.dao.TransferDao;
import org.example.dao.impl.ClientDaoImpl;
import org.example.dao.impl.TransferDaoImpl;
import org.example.model.Client;
import org.example.model.Transfer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class GetClientTransfersCommand extends Command {
    private final ClientDao clientDao = ClientDaoImpl.getInstance();
    private final TransferDao transferDao = TransferDaoImpl.getInstance();
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

        String startDateStr;
        while (true) {
            System.out.println("Enter period start date dd-MMM-yyyy: ");
            try {
                startDateStr = scanner.next("dd-MMM-yyyy");
                break;
            } catch (NoSuchElementException e) {
                System.out.println("Period start date should be in dd-MMM-yyyy format");
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

        String endDateStr;
        while (true) {
            System.out.println("Enter period end date dd-MMM-yyyy: ");
            try {
                endDateStr = scanner.next("dd-MMM-yyyy");
                break;
            } catch (NoSuchElementException e) {
                System.out.println("Period end date should be in dd-MMM-yyyy format");
            }
        }
        try {
            final Date startDate = formatter.parse(startDateStr);
            final Date endDate = formatter.parse(endDateStr);
            final List<Transfer> transfers = transferDao.getClientTransfersForPeriod(clientId, startDate, endDate);
            System.out.println("All transfers for client: " + clientId);
            transfers.forEach(transfer -> System.out.println("Id: " + transfer.getId() + ". Sender Account Id: " + transfer.getSenderAccountId()
                                                                     + " Receiver account id: " + transfer.getReceiverAccountId() + " Amount: " + transfer.getAmount() + " Date: " + transfer.getDate()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
