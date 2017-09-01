package org.openhab.binding.zonky.internal.model;

public class ZonkyCurrentOverview {
    private int totalInvestment;
    private int principalPaid;
    private int interestPaid;
    private int investmentCount;
    private int principalLeft;
    private int principalLeftToPay;
    private int principalLeftDue;
    private int interestPlanned;
    private int interestLeft;
    private int interestLeftToPay;
    private int interestLeftDue;

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

    public int getPrincipalLeft() {
        return principalLeft;
    }

    public int getPrincipalLeftToPay() {
        return principalLeftToPay;
    }

    public int getPrincipalLeftDue() {
        return principalLeftDue;
    }

    public int getInterestPlanned() {
        return interestPlanned;
    }

    public int getInterestLeft() {
        return interestLeft;
    }

    public int getInterestLeftToPay() {
        return interestLeftToPay;
    }

    public int getInterestLeftDue() {
        return interestLeftDue;
    }
}
