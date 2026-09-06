package org.voxelware.coretuff.Features.Economy;

import java.text.DecimalFormat;

public final class CurrencyFormatter {

    private final String symbol;
    private final DecimalFormat format;

    public CurrencyFormatter(String symbol, int decimalPlaces) {
        this.symbol = symbol;
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            pattern.append("0".repeat(decimalPlaces));
        }
        this.format = new DecimalFormat(pattern.toString());
    }

    public String format(double amount) {
        if (amount >= 1_000_000_000) {
            return symbol + format.format(amount / 1_000_000_000D) + "B";
        }
        if (amount >= 1_000_000) {
            return symbol + format.format(amount / 1_000_000D) + "M";
        }
        if (amount >= 1_000) {
            return symbol + format.format(amount / 1_000D) + "K";
        }
        return symbol + format.format(amount);
    }

    public String formatFull(double amount) {
        return symbol + format.format(amount);
    }

    public String symbol() {
        return symbol;
    }
}
