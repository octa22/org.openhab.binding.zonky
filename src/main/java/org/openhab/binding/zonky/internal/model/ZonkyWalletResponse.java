package org.openhab.binding.zonky.internal.model;

public class ZonkyWalletResponse {
    private int id;
    private Number balance;
    private Number availableBalance;
    private Number blockedBalance;
    private Number creditSum;
    private Number debitSum;
    private String variableSymbol;
    private ZonkyAccount account;

    public int getId() {
        return id;
    }

    public Number getBalance() {
        return balance;
    }

    public Number getAvailableBalance() {
        return availableBalance;
    }

    public Number getBlockedBalance() {
        return blockedBalance;
    }

    public Number getCreditSum() {
        return creditSum;
    }

    public Number getDebitSum() {
        return debitSum;
    }

    public String getVariableSymbol() {
        return variableSymbol;
    }

    public ZonkyAccount getAccount() {
        return account;
    }
}
