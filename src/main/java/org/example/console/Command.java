package org.example.console;

import java.util.Scanner;

public abstract class Command {
    protected Scanner scanner = new Scanner(System.in);
    public abstract void execute();
}
