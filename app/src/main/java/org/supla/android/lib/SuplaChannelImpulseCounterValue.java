package org.supla.android.lib;

import java.io.Serializable;

public class SuplaChannelImpulseCounterValue implements Serializable {

    private int ImpulsesPerUnit;
    private long Counter;
    private double CalculatedValue;
    private double TotalCost;
    private double PricePerUnit;
    private String Currency;
    private String Unit;

    SuplaChannelImpulseCounterValue(int ImpulsesPerUnit, long Counter,
                                    long CalculatedValue, int TotalCost, int PricePerUnit,
                                    String Currency, String Unit) {

        this.ImpulsesPerUnit = ImpulsesPerUnit;
        this.Counter = Counter;
        this.CalculatedValue = CalculatedValue / 1000.00;
        this.TotalCost = TotalCost / 100.00;
        this.PricePerUnit = PricePerUnit / 10000.00;
        this.Currency = Currency;
        this.Unit = Unit;
    }

    public int getImpulsesPerUnit() {
        return ImpulsesPerUnit;
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

    public String getUnit() {
        return Unit;
    }
}
