package org.supla.android.lib;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
