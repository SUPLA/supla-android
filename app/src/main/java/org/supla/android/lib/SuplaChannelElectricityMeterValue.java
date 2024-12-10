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

import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.supla.android.data.source.remote.electricitymeter.ElectricityMeterPhaseSequence;
import org.supla.android.tools.UsedFromNativeCode;

public class SuplaChannelElectricityMeterValue implements Serializable {

  private final double totalCost;
  private final List<Summary> sumList;
  private final List<Measurement> mp1List;
  private final List<Measurement> mp2List;
  private final List<Measurement> mp3List;
  private final int measuredValues;
  private final int voltagePhaseAngle12;
  private final int voltagePhaseAngle13;
  private final ElectricityMeterPhaseSequence voltagePhaseSequence;
  private final ElectricityMeterPhaseSequence currentPhaseSequence;
  private final int period;
  private final double pricePerUnit;
  private final String currency;
  // Unit - kWh
  private final double totalForwardActiveEnergyBalanced;
  private final double totalReverseActiveEnergyBalanced;

  @UsedFromNativeCode
  SuplaChannelElectricityMeterValue(
      int MeasuredValues,
      int VoltagePhaseAngle12,
      int VoltagePhaseAngle13,
      int PhaseSequence,
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

    this.measuredValues = MeasuredValues;
    this.period = Period;
    this.totalCost = TotalCost / 100.00;
    this.pricePerUnit = PricePerUnit / 10000.00;
    this.currency = Currency;
    this.totalForwardActiveEnergyBalanced = TotalForwardActiveEnergyBalanced / 100000.00;
    this.totalReverseActiveEnergyBalanced = TotalReverseActiveEnergyBalanced / 100000.00;
    this.voltagePhaseAngle12 = VoltagePhaseAngle12;
    this.voltagePhaseAngle13 = VoltagePhaseAngle13;
    this.voltagePhaseSequence =
        ElectricityMeterPhaseSequence.Companion.from((PhaseSequence & 0x1) == 0);
    this.currentPhaseSequence =
        ElectricityMeterPhaseSequence.Companion.from((PhaseSequence & 0x2) == 0);
  }

  public double getTotalCost() {
    return totalCost;
  }

  @UsedFromNativeCode
  public void addSummary(int Phase, Summary Sum) {
    if (Phase >= 1 && Phase <= 3) {
      if (sumList.size() >= Phase) {
        sumList.set(Phase - 1, Sum);
      }
      sumList.add(Phase - 1, Sum);
    }
  }

  @UsedFromNativeCode
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
    return measuredValues;
  }

  public double getPricePerUnit() {
    return pricePerUnit;
  }

  public String getCurrency() {
    return currency;
  }

  public int getVoltagePhaseAngle12() {
    return voltagePhaseAngle12;
  }

  public int getVoltagePhaseAngle13() {
    return voltagePhaseAngle13;
  }

  public ElectricityMeterPhaseSequence getVoltagePhaseSequence() {
    return voltagePhaseSequence;
  }

  public ElectricityMeterPhaseSequence getCurrentPhaseSequence() {
    return currentPhaseSequence;
  }

  public double getTotalForwardActiveEnergyBalanced() {
    return totalForwardActiveEnergyBalanced;
  }

  public double getTotalReverseActiveEnergyBalanced() {
    return totalReverseActiveEnergyBalanced;
  }

  @Nullable
  public Measurement getMeasurement(int Phase, int index) {
    return switch (Phase) {
      case 1 -> (mp1List.size() > index) ? mp1List.get(index) : null;
      case 2 -> (mp2List.size() > index) ? mp2List.get(index) : null;
      case 3 -> (mp3List.size() > index) ? mp3List.get(index) : null;
      default -> null;
    };
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

  public class Measurement implements Serializable {

    private final double frequency; // Hz
    private final double voltage; // V
    private final double current; // A
    private final double powerActive; // W
    private final double powerReactive; // var
    private final double powerApparent; // VA
    private final double powerFactor;
    private final double phaseAngle;

    @UsedFromNativeCode
    Measurement(
        int frequency,
        int Voltage,
        int Current,
        int PowerActive,
        int PowerReactive,
        int PowerApparent,
        int PowerFactor,
        int PhaseAngle) {
      this.frequency = frequency / 100.00;
      this.voltage = Voltage / 100.00;
      this.current = Current / 1000.00;
      this.powerActive = PowerActive / 100000.00;
      this.powerReactive = PowerReactive / 100000.00;
      this.powerApparent = PowerApparent / 100000.00;
      this.powerFactor = PowerFactor / 1000.00;
      this.phaseAngle = PhaseAngle / 10.00;
    }

    public double getFrequency() {
      return frequency;
    }

    public double getVoltage() {
      return voltage;
    }

    public double getCurrent() {
      return current;
    }

    public double getPowerActive() {
      return powerActive;
    }

    public double getPowerReactive() {
      return powerReactive;
    }

    public double getPowerApparent() {
      return powerApparent;
    }

    public double getPowerFactor() {
      return powerFactor;
    }

    public double getPhaseAngle() {
      return phaseAngle;
    }
  }

  public class Summary implements Serializable {

    // Unit - kWh
    private final double totalForwardActiveEnergy;
    private final double totalReverseActiveEnergy;
    private final double totalForwardReactiveEnergy;
    private final double totalReverseReactiveEnergy;

    @UsedFromNativeCode
    Summary(
        long TotalForwardActiveEnergy,
        long TotalReverseActiveEnergy,
        long TotalForwardReactiveEnergy,
        long TotalReverseReactiveEnergy) {
      this.totalForwardActiveEnergy = TotalForwardActiveEnergy / 100000.00;
      this.totalReverseActiveEnergy = TotalReverseActiveEnergy / 100000.00;
      this.totalForwardReactiveEnergy = TotalForwardReactiveEnergy / 100000.00;
      this.totalReverseReactiveEnergy = TotalReverseReactiveEnergy / 100000.00;
    }

    Summary(
        double TotalForwardActiveEnergy,
        double TotalReverseActiveEnergy,
        double TotalForwardReactiveEnergy,
        double TotalReverseReactiveEnergy) {
      this.totalForwardActiveEnergy = TotalForwardActiveEnergy;
      this.totalReverseActiveEnergy = TotalReverseActiveEnergy;
      this.totalForwardReactiveEnergy = TotalForwardReactiveEnergy;
      this.totalReverseReactiveEnergy = TotalReverseReactiveEnergy;
    }

    public double getTotalForwardActiveEnergy() {
      return totalForwardActiveEnergy;
    }

    public double getTotalReverseActiveEnergy() {
      return totalReverseActiveEnergy;
    }

    public double getTotalForwardReactiveEnergy() {
      return totalForwardReactiveEnergy;
    }

    public double getTotalReverseReactiveEnergy() {
      return totalReverseReactiveEnergy;
    }
  }
}
