package org.supla.android.lib;

import org.supla.android.Trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SuplaChannelElectricityMeter implements Serializable {
    public class Measurement implements Serializable {
        private double Freq; // Hz
        private double Voltage; // V
        private double Current; // A
        private double PowerActive; // W
        private double PowerReactive; // var
        private double PowerApparent; // VA
        private double PowerFactor;
        private double PhaseAngle;

        Measurement(int Freq, int Voltage, int Current,
                    int PowerActive, int PowerReactive,
                    int PowerApparent, int PowerFactor,
                    int PhaseAngle) {
            this.Freq = Freq / 100.00;
            this.Voltage = Voltage / 100.00;
            this.Current = Current / 1000.00;
            this.PowerActive = PowerActive / 100000.00;
            this.PowerReactive = PowerReactive / 100000.00;
            this.PowerApparent = PowerApparent / 100000.00;
            this.PowerFactor = PowerFactor / 1000.00;
            this.PhaseAngle = PhaseAngle / 100000.00;
        }

        double getFreq() { return Freq; };
        double getVoltage() { return Voltage; };
        double getCurrent() { return Current; };
        double getPowerActive() { return PowerActive; };
        double getPowerReactive() { return PowerReactive; };
        double getPowerApparent() { return PowerApparent; };
        double getPowerFactor() { return PowerFactor; };
        double getPhaseAngle() { return PhaseAngle; };
    }

    public class Summary implements Serializable {
        // Unit - kWh
        private double TotalForwardActiveEnergy;
        private double TotalReverseActiveEnergy;
        private double TotalForwardReactiveEnergy;
        private double TotalReverseReactiveEnergy;

        Summary(long TotalForwardActiveEnergy, long TotalReverseActiveEnergy,
                long TotalForwardReactiveEnergy, long TotalReverseReactiveEnergy) {
            this.TotalForwardActiveEnergy = TotalForwardActiveEnergy / 100000.00;
            this.TotalReverseActiveEnergy = TotalReverseActiveEnergy / 100000.00;
            this.TotalForwardReactiveEnergy = TotalForwardReactiveEnergy / 100000.00;
            this.TotalReverseReactiveEnergy = TotalReverseReactiveEnergy / 100000.00;
        }

        double getTotalForwardActiveEnergy() { return TotalForwardActiveEnergy; };
        double getTotalReverseActiveEnergy() { return TotalReverseActiveEnergy; };
        double getTotalForwardReactiveEnergy() { return TotalForwardReactiveEnergy; };
        double getTotalReverseReactiveEnergy() { return TotalReverseReactiveEnergy; };
    }

    private List<Summary>sumList;
    private List<Measurement>mp1List;
    private List<Measurement>mp2List;
    private List<Measurement>mp3List;

    private int MeasuredValues;
    private int Period;

    SuplaChannelElectricityMeter(int MeasuredValues, int Period) {
        sumList = new ArrayList<>();
        mp1List = new ArrayList<>();
        mp2List = new ArrayList<>();
        mp3List = new ArrayList<>();

        this.MeasuredValues = MeasuredValues;
        this.Period = Period;
    }

    public void addSummary(int Phase, Summary Sum) {
        if (Phase >= 1 && Phase <= 3) {
            if (sumList.size() >= Phase) {
                sumList.set(Phase-1, Sum);
            }
            sumList.add(Phase-1, Sum);
        }
    }

    public void addMeasurement(int Phase, Measurement m) {
        switch(Phase) {
            case 1: mp1List.add(m); break;
            case 2: mp2List.add(m); break;
            case 3: mp3List.add(m); break;
        }
    }

    public int getMeasuredValues() {
        return MeasuredValues;
    }

    public int getPeriod() {
        return Period;
    }

    public int getMeasurementSize(int Phase) {
        switch(Phase) {
            case 1: return mp1List.size();
            case 2: return mp2List.size();
            case 3: return mp3List.size();
        }
        return 0;
    }

    public Measurement getMeasurement(int Phase, int index) {
        switch(Phase) {
            case 1: return ( mp1List.size() > index ) ? mp1List.get(index) : null;
            case 2: return ( mp2List.size() > index ) ? mp2List.get(index) : null;
            case 3: return ( mp3List.size() > index ) ? mp3List.get(index) : null;
        }
        return null;
    }

    public Summary getSummary(int Phase) {
        if (Phase >= 1 && Phase <= 3 && sumList.size() >= Phase) {
            return sumList.get(Phase-1);
        }
        return null;
    }

}
