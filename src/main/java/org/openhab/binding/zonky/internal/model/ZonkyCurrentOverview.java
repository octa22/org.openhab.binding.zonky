package org.openhab.binding.zonky.internal.model;

public class ZonkyCurrentOverview {
    private Number totalInvestment;
    private Number principalPaid;
    private Number interestPaid;
    private int investmentCount;
    private Number principalLeft;
    private Number principalLeftToPay;
    private Number principalLeftDue;
    private Number interestPlanned;
    private Number interestLeft;
    private Number interestLeftToPay;
    private Number interestLeftDue;

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

    public Number getPrincipalLeft() {
        return principalLeft;
    }

    public Number getPrincipalLeftToPay() {
        return principalLeftToPay;
    }

    public Number getPrincipalLeftDue() {
        return principalLeftDue;
    }

    public Number getInterestPlanned() {
        return interestPlanned;
    }

    public Number getInterestLeft() {
        return interestLeft;
    }

    public Number getInterestLeftToPay() {
        return interestLeftToPay;
    }

    public Number getInterestLeftDue() {
        return interestLeftDue;
    }
}
