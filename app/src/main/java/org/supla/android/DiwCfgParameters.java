package org.supla.android;

public class DiwCfgParameters extends DeviceCfgParameters {

    public static final int INPUT_MODE_UNKNOWN = -1;
    public static final int INPUT_MODE_MONOSTABLE = 0;
    public static final int INPUT_MODE_BISTABLE = 1;

    public static final int BEHAVIOR_NORMAL = 0;
    public static final int BEHAVIOR_LOOP = 1;

    public static final int BISTABLE_MODE_100P = 0;
    public static final int BISTABLE_MODE_RESTORE = 1;

    private byte cfgVersion;
    private String stmFirmwareVersion = "";
    private short minimum;
    private short maximum;
    private byte dimmingTime;
    private byte lighteeningTime;
    private byte inputTime;
    private byte inputBehavior;
    private byte stateAfterPowerReturn;
    private byte inputMode;
    private byte inputBiMode;

    public boolean setParams(byte[] data) {
        if (data == null || data.length != 67) {
            return false;
        }

        cfgVersion = data[0];
        if (data[1] != 0 || data[2] != 0 || data[3] != 0 || data[4] != 0) {
            stmFirmwareVersion = String.format("%d.%d.%d.%d", data[1], data[2], data[3], data[4]);
        } else {
            stmFirmwareVersion = "";
        }

        minimum = getShort(data, 5);
        maximum = getShort(data, 7);
        dimmingTime = data[9];
        lighteeningTime = data[10];
        inputTime = data[11];
        inputBehavior = data[12];
        stateAfterPowerReturn = data[13];
        inputMode = data[14];
        inputBiMode = data[15];

        setLedConfig(data[16]);
        return true;
    }

    public byte getCfgVersion() {
        return cfgVersion;
    }

    public String getStmFirmwareVersion() {
        return stmFirmwareVersion;
    }

    public short getMinimum() {
        return minimum;
    }

    public short getMaximum() {
        return maximum;
    }

    public byte getDimmingTime() {
        return dimmingTime;
    }

    public byte getLighteeningTime() {
        return lighteeningTime;
    }

    public byte getInputTime() {
        return inputTime;
    }

    public byte getInputBehavior() {
        return inputBehavior;
    }

    public byte getStateAfterPowerReturn() {
        return stateAfterPowerReturn;
    }

    public byte getInputMode() {
        return inputMode;
    }

    public byte getInputBiMode() {
        return inputBiMode;
    }
}
