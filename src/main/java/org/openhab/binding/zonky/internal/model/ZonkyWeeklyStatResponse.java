package org.openhab.binding.zonky.internal.model;

public class ZonkyWeeklyStatResponse {
    private int newInvestments;
    private Number newInvestmentsAmount;
    private int paidInstalments;
    private Number paidInstalmentsAmount;
    private int soldInvestments;
    private Number soldInvestmentsAmount;
    private int boughtInvestments;
    private Number boughtInvestmentsAmount;

    public int getNewInvestments() {
        return newInvestments;
    }

    public Number getNewInvestmentsAmount() {
        return newInvestmentsAmount;
    }

    public int getPaidInstalments() {
        return paidInstalments;
    }

    public Number getPaidInstalmentsAmount() {
        return paidInstalmentsAmount;
    }

    public int getSoldInvestments() {
        return soldInvestments;
    }

    public Number getSoldInvestmentsAmount() {
        return soldInvestmentsAmount;
    }

    public int getBoughtInvestments() {
        return boughtInvestments;
    }

    public Number getBoughtInvestmentsAmount() {
        return boughtInvestmentsAmount;
    }
}
