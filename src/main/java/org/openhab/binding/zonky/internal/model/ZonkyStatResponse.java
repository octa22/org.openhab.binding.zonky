package org.openhab.binding.zonky.internal.model;

public class ZonkyStatResponse {
    private double currentProfitability;
    private double expectedProfitability;
    private ZonkyCurrentOverview currentOverview;
    private ZonkyOverallOverview overallOverview;

    public double getCurrentProfitability() {
        return currentProfitability;
    }

    public double getExpectedProfitability() {
        return expectedProfitability;
    }

    public ZonkyCurrentOverview getCurrentOverview() {
        return currentOverview;
    }

    public ZonkyOverallOverview getOverallOverview() {
        return overallOverview;
    }
}
