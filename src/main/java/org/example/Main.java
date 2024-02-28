package org.example;

import org.example.console.ConsoleInterface;
import org.example.exception.BankAppException;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException, BankAppException {
        ConsoleInterface.run();
    }
}