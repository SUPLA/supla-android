package org.supla.android.lib;

import java.io.Serializable;

public class SuplaChannelImpulseCounterValue implements Serializable {

    private long Counter;
    private double CalculatedValue;
    private double TotalCost;
    private double PricePerUnit;
    private String Currency;

    SuplaChannelImpulseCounterValue(long Counter, long CalculatedValue,
                                    int TotalCost, int PricePerUnit, String Currency) {

        this.Counter = Counter;
        this.CalculatedValue = CalculatedValue / 1000;
        this.TotalCost = TotalCost / 100.00;
        this.PricePerUnit = PricePerUnit / 10000.00;
        this.Currency = Currency;
    }

    public long getCounter() {
        return Counter;
    }

    public double getCalculatedValue() {
        return CalculatedValue;
    }

    public double getTotalCost() {
        return TotalCost;
    }

    public double getPricePerUnit() {
        return PricePerUnit;
    }

    public String getCurrency() {
        return Currency;
    }
}
