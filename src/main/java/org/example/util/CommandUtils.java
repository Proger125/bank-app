package org.example.util;

public class CommandUtils {
    public  static boolean isUsdOrEur(final String currency) {
        return "USD".equals(currency) || "EUR".equals(currency);
    }
}
