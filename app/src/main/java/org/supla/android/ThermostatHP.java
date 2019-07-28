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

public class ThermostatHP {

    private final static int STATUS_POWERON = 0x01;
    private final static int STATUS_PROGRAMMODE = 0x04;
    private final static int STATUS_HEATERANDWATERTEST = 0x10;
    private final static int STATUS_HEATING = 0x20;

    private int presetTemperatureMin;
    private Double measuredTemperatureMin;
    private Double waterMax;
    Double ecoReduction;
    Double comfortTemp;
    Double ecoTemp;
    Integer flags;
    Integer turbo;
    int errors;
    boolean thermostatOn;
    boolean ecoOn;
    boolean turboOn;
    boolean autoOn;
    SuplaChannelThermostatValue.Schedule schedule;

    public ThermostatHP() {
    }

    public ThermostatHP(ChannelExtendedValue cev) {
        assign(cev);
    }

    public ThermostatHP(Cursor cursor) {
        assign(cursor);
    }

    public ThermostatHP(Channel channel) {
        assign(channel);
    }

    public boolean assign(ChannelExtendedValue cev) {

        presetTemperatureMin = 0;
        measuredTemperatureMin = null;
        waterMax = null;
        ecoReduction = null;
        comfortTemp = null;
        ecoTemp = null;
        flags = null;
        turbo = null;
        errors = 0;
        thermostatOn = false;
        ecoOn = false;
        turboOn = false;
        autoOn = false;
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
        ecoReduction = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);
        comfortTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(4);
        ecoTemp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(5);
        flags = cev.getExtendedValue().ThermostatValue.getFlags(4);

        if (flags != null) {
            thermostatOn = (flags & STATUS_POWERON) > 0;
            autoOn = (flags & STATUS_PROGRAMMODE) > 0;
        }

        turbo = cev.getExtendedValue().ThermostatValue.getValues(4);
        if (turbo != null) {
            turboOn = turbo > 0;
        }

        schedule = cev.getExtendedValue().ThermostatValue.getSchedule();
        errors = cev.getExtendedValue().ThermostatValue.getFlags(6);

        return true;
    }

    public boolean assign(Channel channel) {
        ChannelExtendedValue cev = channel == null ? null : channel.getExtendedValue();
        return assign(cev);
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

    public Double getEcoReduction() {
        return ecoReduction;
    }

    public Double getComfortTemp() {
        return comfortTemp;
    }

    public Double getEcoTemp() {
        return ecoTemp;
    }

    public Integer getFlags() {
        return flags;
    }

    public Integer getTurbo() {
        return turbo;
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
        return thermostatOn;
    }

    public boolean isNormalOn() {
        return isThermostatOn() && !isEcoOn() && !isTurboOn() && !isAutoOn();
    }

    public boolean isEcoOn() {
        return ecoOn;
    }

    public boolean isTurboOn() {
        return turboOn;
    }

    public boolean isAutoOn() {
        return autoOn;
    }

    public SuplaChannelThermostatValue.Schedule getSchedule() {
        return schedule;
    }
}
