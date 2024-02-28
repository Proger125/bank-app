package org.example.console.impl;

import org.example.console.Command;

public class ExitCommand extends Command {
    @Override
    public void execute() {
        System.exit(0);
    }
}
