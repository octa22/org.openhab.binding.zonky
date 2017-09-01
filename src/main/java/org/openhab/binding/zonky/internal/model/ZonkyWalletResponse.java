package org.openhab.binding.zonky.internal.model;

public class ZonkyWalletResponse {
    private int id;
    private double balance;
    private double availableBalance;
    private double blockedBalance;
    private double creditSum;
    private double debitSum;
    private String variableSymbol;
    private ZonkyAccount account;

    public int getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getBlockedBalance() {
        return blockedBalance;
    }

    public double getCreditSum() {
        return creditSum;
    }

    public double getDebitSum() {
        return debitSum;
    }

    public String getVariableSymbol() {
        return variableSymbol;
    }

    public ZonkyAccount getAccount() {
        return account;
    }
}
