package org.openhab.binding.zonky.internal.model;

public class ZonkyStatResponse {
    private Number currentProfitability;
    private Number expectedProfitability;
    private ZonkyCurrentOverview currentOverview;
    private ZonkyOverallOverview overallOverview;

    public Number getCurrentProfitability() {
        return currentProfitability;
    }

    public Number getExpectedProfitability() {
        return expectedProfitability;
    }

    public ZonkyCurrentOverview getCurrentOverview() {
        return currentOverview;
    }

    public ZonkyOverallOverview getOverallOverview() {
        return overallOverview;
    }
}
