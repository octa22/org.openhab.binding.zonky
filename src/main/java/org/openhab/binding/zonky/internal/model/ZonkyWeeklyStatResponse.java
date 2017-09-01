package org.openhab.binding.zonky.internal.model;

public class ZonkyWeeklyStatResponse {
    private int newInvestments;
    private double newInvestmentsAmount;
    private int paidInstalments;
    private double paidInstalmentsAmount;
    private int soldInvestments;
    private double soldInvestmentsAmount;
    private int boughtInvestments;
    private double boughtInvestmentsAmount;

    public int getNewInvestments() {
        return newInvestments;
    }

    public double getNewInvestmentsAmount() {
        return newInvestmentsAmount;
    }

    public int getPaidInstalments() {
        return paidInstalments;
    }

    public double getPaidInstalmentsAmount() {
        return paidInstalmentsAmount;
    }

    public int getSoldInvestments() {
        return soldInvestments;
    }

    public double getSoldInvestmentsAmount() {
        return soldInvestmentsAmount;
    }

    public int getBoughtInvestments() {
        return boughtInvestments;
    }

    public double getBoughtInvestmentsAmount() {
        return boughtInvestmentsAmount;
    }
}
