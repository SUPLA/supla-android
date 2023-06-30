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
import java.util.ArrayList;
import java.util.List;

public class SuplaChannelElectricityMeterValue implements Serializable {
  private double TotalCost;
  private List<Summary> sumList;
  private List<Measurement> mp1List;
  private List<Measurement> mp2List;
  private List<Measurement> mp3List;
  private int MeasuredValues;
  private int Period;
  private double PricePerUnit;
  private String Currency;
  // Unit - kWh
  private double TotalForwardActiveEnergyBalanced;
  private double TotalReverseActiveEnergyBalanced;

  SuplaChannelElectricityMeterValue(
      int MeasuredValues,
      int Period,
      int TotalCost,
      int PricePerUnit,
      String Currency,
      long TotalForwardActiveEnergyBalanced,
      long TotalReverseActiveEnergyBalanced) {
    sumList = new ArrayList<>();
    mp1List = new ArrayList<>();
    mp2List = new ArrayList<>();
    mp3List = new ArrayList<>();

    this.MeasuredValues = MeasuredValues;
    this.Period = Period;
    this.TotalCost = TotalCost / 100.00;
    this.PricePerUnit = PricePerUnit / 10000.00;
    this.Currency = Currency;
    this.TotalForwardActiveEnergyBalanced = TotalForwardActiveEnergyBalanced / 100000.00;
    this.TotalReverseActiveEnergyBalanced = TotalReverseActiveEnergyBalanced / 100000.00;
  }

  SuplaChannelElectricityMeterValue(
      int MeasuredValues,
      int Period,
      double TotalCost,
      double PricePerUnit,
      String Currency,
      double TotalForwardActiveEnergyBalanced,
      double TotalReverseActiveEnergyBalanced) {
    sumList = new ArrayList<>();
    mp1List = new ArrayList<>();
    mp2List = new ArrayList<>();
    mp3List = new ArrayList<>();

    this.MeasuredValues = MeasuredValues;
    this.Period = Period;
    this.TotalCost = TotalCost;
    this.PricePerUnit = PricePerUnit;
    this.Currency = Currency;
    this.TotalForwardActiveEnergyBalanced = TotalForwardActiveEnergyBalanced;
    this.TotalReverseActiveEnergyBalanced = TotalReverseActiveEnergyBalanced;
  }

  public double getTotalCost() {
    return TotalCost;
  }

  public void addSummary(int Phase, Summary Sum) {
    if (Phase >= 1 && Phase <= 3) {
      if (sumList.size() >= Phase) {
        sumList.set(Phase - 1, Sum);
      }
      sumList.add(Phase - 1, Sum);
    }
  }

  public void addMeasurement(int Phase, Measurement m) {
    switch (Phase) {
      case 1:
        mp1List.add(m);
        break;
      case 2:
        mp2List.add(m);
        break;
      case 3:
        mp3List.add(m);
        break;
    }
  }

  public int getMeasuredValues() {
    return MeasuredValues;
  }

  public boolean currentIsOver65A() {
    return (getMeasuredValues() & SuplaConst.EM_VAR_CURRENT_OVER_65A) > 0
        && (getMeasuredValues() & SuplaConst.EM_VAR_CURRENT) == 0;
  }

  public int getPeriod() {
    return Period;
  }

  public double getPricePerUnit() {
    return PricePerUnit;
  }

  public String getCurrency() {
    return Currency;
  }

  public double getTotalForwardActiveEnergyBalanced() {
    return TotalForwardActiveEnergyBalanced;
  }

  public double getTotalReverseActiveEnergyBalanced() {
    return TotalReverseActiveEnergyBalanced;
  }

  public int getMeasurementSize(int Phase) {
    switch (Phase) {
      case 1:
        return mp1List.size();
      case 2:
        return mp2List.size();
      case 3:
        return mp3List.size();
    }
    return 0;
  }

  public Measurement getMeasurement(int Phase, int index) {
    switch (Phase) {
      case 1:
        return (mp1List.size() > index) ? mp1List.get(index) : null;
      case 2:
        return (mp2List.size() > index) ? mp2List.get(index) : null;
      case 3:
        return (mp3List.size() > index) ? mp3List.get(index) : null;
    }
    return null;
  }

  public Summary getSummary() {
    Summary SummaryP1 = getSummary(1);
    Summary SummaryP2 = getSummary(2);
    Summary SummaryP3 = getSummary(3);

    return new Summary(
        SummaryP1.getTotalForwardActiveEnergy()
            + SummaryP2.getTotalForwardActiveEnergy()
            + SummaryP3.getTotalForwardActiveEnergy(),
        SummaryP1.getTotalReverseActiveEnergy()
            + SummaryP2.getTotalReverseActiveEnergy()
            + SummaryP3.getTotalReverseActiveEnergy(),
        SummaryP1.getTotalForwardReactiveEnergy()
            + SummaryP2.getTotalForwardReactiveEnergy()
            + SummaryP3.getTotalForwardReactiveEnergy(),
        SummaryP1.getTotalReverseReactiveEnergy()
            + SummaryP2.getTotalReverseReactiveEnergy()
            + SummaryP3.getTotalReverseReactiveEnergy());
  }

  public Summary getSummary(int Phase) {
    if (Phase >= 1 && Phase <= 3) {
      if (sumList.size() >= Phase) {
        return sumList.get(Phase - 1);
      }
      return new Summary(0.00, 0.00, 0.00, 0.00);
    }
    return null;
  }

  public double[] getTotalActiveEnergyForAllPhases(boolean reverse) {
    double result[] = new double[3];
    for (int a = 0; a < 3; a++) {
      if (reverse) {
        result[a] = getSummary(a + 1).getTotalReverseActiveEnergy();
      } else {
        result[a] = getSummary(a + 1).getTotalForwardActiveEnergy();
      }
    }
    return result;
  }

  public class Measurement implements Serializable {
    private double Freq; // Hz
    private double Voltage; // V
    private double Current; // A
    private double PowerActive; // W
    private double PowerReactive; // var
    private double PowerApparent; // VA
    private double PowerFactor;
    private double PhaseAngle;

    Measurement(
        int Freq,
        int Voltage,
        int Current,
        int PowerActive,
        int PowerReactive,
        int PowerApparent,
        int PowerFactor,
        int PhaseAngle) {
      this.Freq = Freq / 100.00;
      this.Voltage = Voltage / 100.00;
      this.Current = Current / 1000.00;
      this.PowerActive = PowerActive / 100000.00;
      this.PowerReactive = PowerReactive / 100000.00;
      this.PowerApparent = PowerApparent / 100000.00;
      this.PowerFactor = PowerFactor / 1000.00;
      this.PhaseAngle = PhaseAngle / 10.00;
    }

    public double getFreq() {
      return Freq;
    }

    public double getVoltage() {
      return Voltage;
    }

    public double getCurrent(boolean over65A) {
      return Current * (over65A ? 10 : 1);
    }

    public double getPowerActive() {
      return PowerActive;
    }

    public double getPowerReactive() {
      return PowerReactive;
    }

    public double getPowerApparent() {
      return PowerApparent;
    }

    public double getPowerFactor() {
      return PowerFactor;
    }

    public double getPhaseAngle() {
      return PhaseAngle;
    }
  }

  public class Summary implements Serializable {
    // Unit - kWh
    private double TotalForwardActiveEnergy;
    private double TotalReverseActiveEnergy;
    private double TotalForwardReactiveEnergy;
    private double TotalReverseReactiveEnergy;

    Summary(
        long TotalForwardActiveEnergy,
        long TotalReverseActiveEnergy,
        long TotalForwardReactiveEnergy,
        long TotalReverseReactiveEnergy) {
      this.TotalForwardActiveEnergy = TotalForwardActiveEnergy / 100000.00;
      this.TotalReverseActiveEnergy = TotalReverseActiveEnergy / 100000.00;
      this.TotalForwardReactiveEnergy = TotalForwardReactiveEnergy / 100000.00;
      this.TotalReverseReactiveEnergy = TotalReverseReactiveEnergy / 100000.00;
    }

    Summary(
        double TotalForwardActiveEnergy,
        double TotalReverseActiveEnergy,
        double TotalForwardReactiveEnergy,
        double TotalReverseReactiveEnergy) {
      this.TotalForwardActiveEnergy = TotalForwardActiveEnergy;
      this.TotalReverseActiveEnergy = TotalReverseActiveEnergy;
      this.TotalForwardReactiveEnergy = TotalForwardReactiveEnergy;
      this.TotalReverseReactiveEnergy = TotalReverseReactiveEnergy;
    }

    public double getTotalForwardActiveEnergy() {
      return TotalForwardActiveEnergy;
    }

    public double getTotalReverseActiveEnergy() {
      return TotalReverseActiveEnergy;
    }

    public double getTotalForwardReactiveEnergy() {
      return TotalForwardReactiveEnergy;
    }

    public double getTotalReverseReactiveEnergy() {
      return TotalReverseReactiveEnergy;
    }
  }
}
