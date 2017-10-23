package org.openhab.binding.zonky.internal.model;

public class ZonkyOverallOverview {
    private Number totalInvestment;
    private Number principalPaid;
    private Number interestPaid;
    private int  investmentCount;
    private Number feesAmount;
    private Number netIncome;
    private Number principalLost;

    public Number getTotalInvestment() {
        return totalInvestment;
    }

    public Number getPrincipalPaid() {
        return principalPaid;
    }

    public Number getInterestPaid() {
        return interestPaid;
    }

    public int getInvestmentCount() {
        return investmentCount;
    }

    public Number getFeesAmount() {
        return feesAmount;
    }

    public Number getNetIncome() {
        return netIncome;
    }

    public Number getPrincipalLost() {
        return principalLost;
    }
}
