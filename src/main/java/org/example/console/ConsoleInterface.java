package org.example.console;

import org.example.console.impl.AddAccountCommand;
import org.example.console.impl.AddBankCommand;
import org.example.console.impl.AddClientCommand;
import org.example.console.impl.ExitCommand;
import org.example.console.impl.GetClientAccountsCommand;
import org.example.console.impl.GetClientTransfersCommand;
import org.example.console.impl.TransferCommand;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class ConsoleInterface {
    private static final Map<Integer, Command> commands = Map.of(1, new AddBankCommand(), 2, new AddClientCommand(), 3, new AddAccountCommand(), 4,
                                                    new GetClientAccountsCommand(), 5, new GetClientTransfersCommand(), 6, new TransferCommand(), 7, new ExitCommand());
    public static void run() {
        while (true) {
            System.out.println("Choose one command:");
            System.out.println("1. Add bank");
            System.out.println("2. Add client");
            System.out.println("3. Add account");
            System.out.println("4. Get client accounts");
            System.out.println("5. Get client transfers");
            System.out.println("6. Transfer");
            System.out.println("7. Exit");
            final Scanner scanner = new Scanner(System.in);
            try {
                int commandId = scanner.nextInt();
                if (commandId > 0 && commandId < 8) {
                    final Command command = commands.get(commandId);
                    command.execute();
                }
            } catch (InputMismatchException e) {
                System.out.println("Command should be integer");
            }

        }
    }
}
