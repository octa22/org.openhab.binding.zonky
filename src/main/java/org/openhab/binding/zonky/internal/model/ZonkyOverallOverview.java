package org.openhab.binding.zonky.internal.model;

public class ZonkyOverallOverview {
    private int totalInvestment;
    private int principalPaid;
    private int interestPaid;
    private int investmentCount;
    private int feesAmount;
    private int netIncome;
    private int principalLost;

    public int getTotalInvestment() {
        return totalInvestment;
    }

    public int getPrincipalPaid() {
        return principalPaid;
    }

    public int getInterestPaid() {
        return interestPaid;
    }

    public int getInvestmentCount() {
        return investmentCount;
    }

    public int getFeesAmount() {
        return feesAmount;
    }

    public int getNetIncome() {
        return netIncome;
    }

    public int getPrincipalLost() {
        return principalLost;
    }
}
