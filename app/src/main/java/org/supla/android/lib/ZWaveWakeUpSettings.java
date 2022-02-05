package org.supla.android.lib;

public class ZWaveWakeUpSettings {
    private int minimum;
    private int maximum;
    private int value;
    private int step;

    ZWaveWakeUpSettings(int minimum, int maximum, int value, int step) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = value;
        this.step = step;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getStep() {
        return step;
    }
}
