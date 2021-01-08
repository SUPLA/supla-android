package org.supla.android;

public class DeviceCfgParameters {
    public final static int LED_UNKNOWN = -1;
    public final static int LED_ON_WHEN_CONNECTED = 0;
    public final static int LED_OFF_WHEN_CONNECTED = 1;
    public final static int LED_ALWAYS_OFF = 2;

    private Byte LedConfig = null;

    protected short getShort(byte[] data, int offset) {
        int x = (int) data[offset + 1] & 0xFF;
        x <<= 8;
        x |= (int) data[offset] & 0xFF;
        return (short) x;
    }

    public Byte getLedConfig() {
        return LedConfig;
    }

    protected void setLedConfig(Byte ledConfig) {
        LedConfig = ledConfig;
    }
}
