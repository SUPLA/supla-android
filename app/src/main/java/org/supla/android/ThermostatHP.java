package org.supla.android;

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

import android.content.Context;
import android.database.Cursor;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaChannelThermostatValue;

import java.lang.annotation.Target;

public class ThermostatHP {

    private final static int STATUS_POWERON = 0x01;
    private final static int STATUS_PROGRAMMODE = 0x04;
    private final static int STATUS_HEATERANDWATERTEST = 0x10;
    private final static int STATUS_HEATING = 0x20;

    private final static int STATUS2_TURBO_ON = 0x1;
    private final static int STATUS2_ECOREDUCTION_ON = 0x2;

    private int presetTemperatureMin;
    private Double measuredTemperatureMin;
    private Double waterMax;
    private boolean online;
    Double ecoReductionTemperature;
    Double comfortTemp;
    Double ecoTemp;
    Integer flags1;
    Integer flags2;
    Integer turboTime;
    int errors;

    SuplaChannelThermostatValue.Schedule schedule;

    public ThermostatHP() {
    }

    public ThermostatHP(ChannelExtendedValue cev, boolean online) {
        assign(cev, online);
    }

    public ThermostatHP(Cursor cursor) {
        assign(cursor);
    }

    public ThermostatHP(Channel channel) {
        assign(channel);
    }

    public boolean assign(ChannelExtendedValue cev, boolean online) {

        this.online = online;
        presetTemperatureMin = 0;
        measuredTemperatureMin = null;
        waterMax = null;
        ecoReductionTemperature = null;
        comfortTemp = null;
        ecoTemp = null;
        flags1 = null;
        flags2 = null;
        turboTime = null;
        errors = 0;
        schedule = null;

        if (cev == null
                || cev.getType() != SuplaConst.EV_TYPE_THERMOSTAT_DETAILS_V1
                || cev.getExtendedValue().ThermostatValue == null) {
            return false;
        }

        Double temp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(0);
        presetTemperatureMin = temp != null ? temp.intValue() : 0;
        measuredTemperatureMin = cev.getExtendedValue().ThermostatValue.getMeasuredTemperature(0);
        waterMax = cev.getExtendedValue().ThermostatValue.getPresetTemperature(2);
        ecoReductionTemperature = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);
        comfortTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(4);
        ecoTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(5);
        flags1 = cev.getExtendedValue().ThermostatValue.getFlags(4);
        turboTime = cev.getExtendedValue().ThermostatValue.getValues(4);
        schedule = cev.getExtendedValue().ThermostatValue.getSchedule();
        errors = cev.getExtendedValue().ThermostatValue.getFlags(6);
        flags2 = cev.getExtendedValue().ThermostatValue.getFlags(7);

        return true;
    }

    public boolean assign(Channel channel) {
        ChannelExtendedValue cev = channel == null ? null : channel.getExtendedValue();
        return assign(cev, channel.getOnLine());
    }

    public boolean assign(Cursor cursor) {
        Channel channel = new Channel();
        channel.AssignCursorData(cursor);
        return assign(channel);
    }

    public int getPresetTemperatureMin() {
        return presetTemperatureMin;
    }

    public Double getMeasuredTemperatureMin() {
        return measuredTemperatureMin;
    }

    public Double getWaterMax() {
        return waterMax;
    }

    public Double getEcoReductionTemperature() {
        return ecoReductionTemperature;
    }

    public Double getComfortTemp() {
        return comfortTemp;
    }

    public Double getEcoTemp() {
        return ecoTemp;
    }

    public Integer getFlags1() {
        return flags1;
    }

    public Integer getFlags2() {
        return flags2;
    }

    public Integer getTurboTime() {
        return turboTime;
    }

    public int getErrors() {
        return errors;
    }

    public int getError() {
        if ((errors & 0x1) > 0) {
            return 1;
        } else if ((errors & 0x2) > 0) {
            return 2;
        } else if ((errors & 0x4) > 0) {
            return 3;
        } else if ((errors & 0x8) > 0) {
            return 4;
        } else if ((errors & 0x10) > 0) {
            return 5;
        }
        return 0;
    }

    public String getErrorMessage(Context context) {
        int msgId = 0;
        switch (getError()) {
            case 1:
                msgId = R.string.hp_error_1;
                break;
            case 2:
                msgId = R.string.hp_error_2;
                break;
            case 3:
                msgId = R.string.hp_error_3;
                break;
            case 4:
                msgId = R.string.hp_error_4;
                break;
            case 5:
                msgId = R.string.hp_error_5;
                break;
        }

        if (msgId!=0) {
            context.getResources().getString(msgId);
        }

        return "";
    }

    public boolean isThermostatOn() {
        return online && flags1 != null && (flags1 & STATUS_POWERON) > 0;
    }

    public boolean isNormalOn() {
        return online && isThermostatOn() && !isEcoRecuctionApplied() && !isTurboOn() && !isAutoOn();
    }

    public boolean isEcoRecuctionApplied() {
        return online && flags2 != null && (flags2 & STATUS2_ECOREDUCTION_ON) > 0;
    }

    public boolean isTurboOn() {
        return online && flags2 != null && (flags2 & STATUS2_TURBO_ON) > 0;
    }

    public boolean isAutoOn() {
        return online && flags1 != null && (flags1 & STATUS_PROGRAMMODE) > 0;
    }

    public SuplaChannelThermostatValue.Schedule getSchedule() {
        return schedule;
    }
}
