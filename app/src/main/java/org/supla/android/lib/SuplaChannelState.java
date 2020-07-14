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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import org.supla.android.R;

import java.io.Serializable;

@SuppressWarnings("unused")
public class SuplaChannelState implements Serializable {

    public static final int FIELD_IPV4 = 0x0001;
    public static final int FIELD_MAC = 0x0002;
    public static final int FIELD_BATTERYLEVEL = 0x0004;
    public static final int FIELD_BATTERYPOWERED = 0x0008;
    public static final int FIELD_WIFIRSSI = 0x0010;
    public static final int FIELD_WIFISIGNALSTRENGTH = 0x0020;
    public static final int FIELD_BRIDGENODESIGNALSTRENGTH = 0x0040;
    public static final int FIELD_UPTIME = 0x0080;
    public static final int FIELD_CONNECTIONUPTIME = 0x0100;
    public static final int FIELD_BATTERYHEALTH = 0x0200;
    public static final int FIELD_BRIDGENODEONLINE = 0x0400;
    public static final int FIELD_LASTCONNECTIONRESETCAUSE = 0x0800;
    public static final int FIELD_LIGHTSOURCELIFESPAN = 0x1000;
    public static final int FIELD_LIGHTSOURCELIFEOPERATINGTIME = 0x2000;

    private int ChannelID;
    private int fields;
    private int defaultIconField;
    private Integer ipv4;
    private byte[] macAddress;
    private Byte batteryLevel;
    private Boolean batteryPowered;
    private Byte wiFiRSSI;
    private Byte wiFiSignalStrength;
    private Boolean bridgeNodeOnline;
    private Byte bridgeNodeSignalStrength;
    private Long uptime;
    private Long connectionUptime;
    private Byte batteryHealth;
    private Byte lastConnectionResetCause;
    private Integer lightSourceLifespan;
    private Float lightSourceLifespanLeft;
    private Integer lightSourceOperatingTime;

    public SuplaChannelState(int ChannelID, int fields, int defaultIconField,
                             int ipv4, byte[] macAddress, byte batteryLevel,
                             byte batteryPowered, byte wiFiRSSI, byte wiFiSignalStrength,
                             byte bridgeNodeOnline, byte bridgeNodeSignalStrength, int uptime,
                             int connectionUptime, byte batteryHealth,
                             byte lastConnectionResetCause, int lightSourceLifespan,
                             int lightSourceLifespanLeft) {

        this.ChannelID = ChannelID;
        this.fields = fields;
        this.defaultIconField = defaultIconField;

        if ((fields & FIELD_IPV4) > 0) {
            this.ipv4 = ipv4;
        }

        if ((fields & FIELD_MAC) > 0) {
            this.macAddress = macAddress;
        }

        if ((fields & FIELD_BATTERYLEVEL) > 0
                && batteryLevel >= 0
                && batteryLevel <= 100) {
            this.batteryLevel = batteryLevel;
        }

        if ((fields & FIELD_BATTERYPOWERED) > 0) {
            this.batteryPowered = batteryPowered > 0;
        }

        if ((fields & FIELD_WIFIRSSI) > 0) {
            this.wiFiRSSI = wiFiRSSI;
        }

        if ((fields & FIELD_WIFISIGNALSTRENGTH) > 0
                && wiFiSignalStrength >= 0
                && wiFiSignalStrength <= 100) {
            this.wiFiSignalStrength = wiFiSignalStrength;
        }

        if ((fields & FIELD_BRIDGENODEONLINE) > 0) {
            this.bridgeNodeOnline = bridgeNodeOnline > 0;
        }

        if ((fields & FIELD_BRIDGENODESIGNALSTRENGTH) > 0
                && bridgeNodeSignalStrength >= 0
                && bridgeNodeSignalStrength <= 100) {
            this.bridgeNodeSignalStrength = bridgeNodeSignalStrength;
        }

        if ((fields & FIELD_UPTIME) > 0) {
            this.uptime = uptime & 0x00000000ffffffffL;
        }

        if ((fields & FIELD_CONNECTIONUPTIME) > 0) {
            this.connectionUptime = connectionUptime & 0x00000000ffffffffL;
        }

        if ((fields & FIELD_BATTERYHEALTH) > 0
                && batteryHealth >= 0
                && batteryHealth <= 100) {
            this.batteryHealth = batteryHealth;
        }

        if ((fields & FIELD_LASTCONNECTIONRESETCAUSE) > 0) {
            this.lastConnectionResetCause = lastConnectionResetCause;
        }

        if ((fields & FIELD_LIGHTSOURCELIFESPAN) > 0) {
            this.lightSourceLifespanLeft = lightSourceLifespanLeft / 100f;
            this.lightSourceLifespan = lightSourceLifespan;
        }

        if ((fields & FIELD_LIGHTSOURCELIFEOPERATINGTIME) > 0) {
            this.lightSourceLifespanLeft = null;
            this.lightSourceOperatingTime = lightSourceLifespanLeft;
        }
    }

    public int getChannelID() {
        return ChannelID;
    }

    public Integer getIpv4() {
        return ipv4;
    }

    @SuppressLint("DefaultLocale")
    public String getIpv4String() {
        int ipv4 = this.ipv4.intValue();
        return String.format(
                "%d.%d.%d.%d",
                (ipv4 & 0xff),
                (ipv4 >> 8 & 0xff),
                (ipv4 >> 16 & 0xff),
                (ipv4 >> 24 & 0xff));
    }

    public byte[] getMacAddress() {
        return macAddress;
    }

    public String getMacAddressString() {
        StringBuilder sb = new StringBuilder(macAddress.length * 3 - 1);
        for (byte b : macAddress) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public Byte getBatteryLevel() {
        return batteryLevel;
    }

    public String getBatteryLevelString() {
        return String.format("%d%%", getBatteryLevel());
    }

    public Boolean isBatteryPowered() {
        return batteryPowered;
    }

    public String getBatteryPoweredString(Context context) {
        Resources r = context.getResources();
        return r.getString(isBatteryPowered() ? R.string.yes : R.string.no);
    }

    public Byte getWiFiSignalStrength() {
        return wiFiSignalStrength;
    }

    public String getWiFiSignalStrengthString() {
        return String.format("%d%%", getWiFiSignalStrength());
    }

    public Byte getWiFiRSSI() {
        return wiFiRSSI;
    }

    public String getWiFiRSSIString() {
        return String.format("%d", getWiFiRSSI());
    }

    public Byte getBridgeNodeSignalStrength() {
        return bridgeNodeSignalStrength;
    }

    public String getBridgeNodeSignalStrengthString(Context context) {
        return String.format("%d%%", getBridgeNodeSignalStrength());
    }

    private String getUptimeString(Long uptime, Context context) {
        long secs = uptime == null ? 0 : uptime;

        return String.format("%d %s %02d:%02d:%02d",
                uptime / 86400,
                context.getResources().getString(R.string.days).toLowerCase(),
                uptime % 86400 / 3600,
                uptime % 86400 % 3600 / 60,
                uptime % 86400 % 3600 % 60);
    }

    public Long getUptime() {
        return uptime;
    }

    public String getUptimeString(Context context) {
        return getUptimeString(uptime, context);
    }

    public Long getConnectionUptime() {
        return connectionUptime;
    }

    public String getConnectionUptimeString(Context context) {
        return getUptimeString(connectionUptime, context);
    }

    public int getFields() {
        return fields;
    }

    public int getDefaultIconField() {
        return defaultIconField;
    }

    public Boolean isBridgeNodeOnline() {
        return bridgeNodeOnline;
    }

    public String getBridgeNodeOnlineString(Context context) {
        Resources r = context.getResources();
        return r.getString(isBridgeNodeOnline() ? R.string.yes : R.string.no);
    }

    public Byte getBatteryHealth() {
        return batteryHealth;
    }

    public String getBatteryHealthString() {
        return String.format("%d%%", getBatteryHealth());
    }

    public Byte getLastConnectionResetCause() {
        return lastConnectionResetCause;
    }

    public String getLastConnectionResetCauseString() {
        switch (getLastConnectionResetCause()) {
            default:
                return String.format("%d", getLastConnectionResetCause());
        }
    }

    public Integer getLightSourceLifespan() {
        return lightSourceLifespan;
    }

    public Float getLightSourceLifespanLeft() {
        return lightSourceLifespanLeft;
    }

    public String getLightSourceLifespanString() {

        Float percentLeft = null;

        if (getLightSourceLifespanLeft() != null) {
            percentLeft = getLightSourceLifespanLeft();
        } else if (getLightSourceOperatingTimePercentLeft() != null) {
            percentLeft = getLightSourceOperatingTimePercentLeft();
        }

        if (percentLeft != null) {
            return String.format("%dh (%.2f%%)",
                    getLightSourceLifespan(),
                    percentLeft);
        }

        return String.format("%dh", getLightSourceLifespan());
    }

    public Integer getLightSourceOperatingTime() {
        return lightSourceOperatingTime;
    }

    public Float getLightSourceOperatingTimePercent() {
        if (getLightSourceLifespan() != null && getLightSourceLifespan().intValue() > 0) {
            return getLightSourceOperatingTime() / 36f / getLightSourceLifespan();
        }
        return null;
    }

    public Float getLightSourceOperatingTimePercentLeft() {
        Float percent = getLightSourceOperatingTimePercent();
        if (percent != null) {
            return 100 - percent;
        }
        return null;
    }

    public String getLightSourceOperatingTimeString() {
        long timeSec = getLightSourceOperatingTime().longValue();

        return String.format("%02dh %02d:%02d",
                timeSec / 3600,
                timeSec % 3600 / 60,
                timeSec % 3600 % 60);

    }
}
