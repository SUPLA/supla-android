package org.supla.android.db;

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
import org.supla.android.R;
import org.supla.android.lib.SuplaChannelBase;
import org.supla.android.lib.SuplaConst;

public abstract class ChannelBase extends DbItem {

    private int RemoteId; // SuplaChannelBase.Id
    private String Caption;
    private int Func;
    private int Visible;
    private long LocationId;
    private int AltIcon;
    private int Flags;

    public void setRemoteId(int id) {
        RemoteId = id;
    }

    public int getRemoteId() {
        return RemoteId;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getCaption() {
        return Caption;
    }

    public void setVisible(int visible) {
        Visible = visible;
    }

    public int getVisible() {
        return Visible;
    }

    public void setLocationId(long locationId) {
        LocationId = locationId;
    }

    public long getLocationId() {
        return LocationId;
    }

    public void setFunc(int func) {
        Func = func;
    }

    public int getFunc() {
        return Func;
    }

    protected abstract int _getOnLine();

    public int getOnLinePercent() {
        int result = _getOnLine();
        if ( result > 100 ) {
            result = 100;
        } else if ( result < 0 ) {
            result = 0;
        }
        return result;
    }

    public boolean getOnLine() {
        return getOnLinePercent() > 0;
    }

    public void setAltIcon(int altIcon) {
        AltIcon = altIcon;
    }

    public int getAltIcon() {
        return AltIcon;
    }

    public void setFlags(int flags) {
        Flags = flags;
    }

    public int getFlags() {
        return Flags;
    }

    public String getNotEmptyCaption(Context context) {

        if (context != null && Caption.equals("")) {

            int idx = -1;

            switch (Func) {
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                    idx = R.string.channel_func_gatewayopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                    idx = R.string.channel_func_gateway;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                    idx = R.string.channel_func_gateopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                    idx = R.string.channel_func_gate;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                    idx = R.string.channel_func_garagedooropeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                    idx = R.string.channel_func_garagedoor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                    idx = R.string.channel_func_dooropeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                    idx = R.string.channel_func_door;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
                    idx = R.string.channel_func_rsopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                    idx = R.string.channel_func_rollershutter;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                    idx = R.string.channel_func_powerswith;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                    idx = R.string.channel_func_lightswith;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                    idx = R.string.channel_func_thermometer;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
                    idx = R.string.channel_func_humidity;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                    idx = R.string.channel_func_humidityandtemperature;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                    idx = R.string.channel_func_noliquidsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                    idx = R.string.channel_func_dimmer;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    idx = R.string.channel_func_rgblighting;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                    idx = R.string.channel_func_dimmerandrgblighting;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
                    idx = R.string.channel_func_depthsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
                    idx = R.string.channel_func_distancesensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
                    idx = R.string.channel_func_windowopeningsensor;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:
                    idx = R.string.channel_func_mailsensor;
                    break;
            }

            if (idx == -1)
                Caption = Integer.toString(Func);
            else
                Caption = context.getResources().getString(idx);
        }

        return Caption;
    }


    public void Assign(SuplaChannelBase base) {

        setRemoteId(base.Id);
        setCaption(base.Caption);
        setFunc(base.Func);
        setFlags(base.Flags);
        setAltIcon(base.AltIcon);

    }

    public boolean Diff(SuplaChannelBase base) {

        return base.Id != getRemoteId()
                || !base.Caption.equals(getCaption())
                || base.OnLine != getOnLine()
                || base.Flags != getFlags()
                || base.AltIcon != getAltIcon();

    }

    public boolean Diff(ChannelBase base) {

        return base.getRemoteId() != getRemoteId()
                || !base.getCaption().equals(getCaption())
                || base.getOnLine() != getOnLine()
                || base.getFlags() != getFlags()
                || base.getAltIcon() != getAltIcon();
    }




}
