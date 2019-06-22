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


import android.content.ContentValues;
import android.database.Cursor;

import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaConst;

import java.util.ArrayList;

public class ChannelGroup extends ChannelBase {

    private String TotalValue;
    private int OnLine;

    private int BufferOnLineCount;
    private int BufferOnLine;
    private int BufferCounter;
    private String BufferTotalValue;

    protected int _getOnLine() {
        return OnLine;
    }

    public int getGroupId() {
        return getRemoteId();
    }

    public String getTotalValue() {
        return TotalValue == null ? "" : TotalValue;
    }

    public void AssignCursorData(Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry._ID)));
        setRemoteId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID)));
        setFunc(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE)));
        OnLine = cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION)));
        TotalValue = cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE));
        setLocationId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID)));
        setAltIcon(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON)));
        setUserIconId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON)));
        setFlags(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS)));

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID, getRemoteId());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE, getTotalValue());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE, getOnLinePercent());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC, getFunc());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON, getAltIcon());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON, getUserIconId());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS, getFlags());

        return values;

    }

    public boolean DiffWithBuffer() {

        return OnLine != BufferOnLine
                || TotalValue == null
                || !TotalValue.equals(BufferTotalValue);
    }

    public void resetBuffer() {
        BufferTotalValue = "";
        BufferOnLine = 0;
        BufferOnLineCount = 0;
        BufferCounter = 0;
    }

    public void assignBuffer() {

        OnLine = BufferOnLine;
        TotalValue = BufferTotalValue;

        resetBuffer();
    }

    public void addValueToBuffer(ChannelValue value) {

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                break;
            default:
                return;
        }

        BufferCounter++;
        if (value.getOnLine()) {
            BufferOnLineCount++;
        }

        BufferOnLine = BufferOnLineCount * 100 / BufferCounter;

        if (!value.getOnLine()) {
            return;
        }

        if (!BufferTotalValue.isEmpty()) {
            BufferTotalValue += "|";
        }

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                BufferTotalValue += Integer.toString((value.getSubValueHi() & 0x1) == 0x1 ? 1 : 0);
                break;
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                BufferTotalValue += Integer.toString(value.hiValue() ? 1 : 0);
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                BufferTotalValue += Integer.toString(value.getPercent());
                BufferTotalValue += ":";
                BufferTotalValue += Integer.toString((value.getSubValueHi() & 0x1) == 0x1 ? 1 : 0);
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                BufferTotalValue += Integer.toString(value.getBrightness());
                break;
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                BufferTotalValue += Integer.toString(value.getColor());
                BufferTotalValue += ":";
                BufferTotalValue += Integer.toString(value.getColorBrightness());
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                BufferTotalValue += Integer.toString(value.getColor());
                BufferTotalValue += ":";
                BufferTotalValue += Integer.toString(value.getColorBrightness());
                BufferTotalValue += ":";
                BufferTotalValue += Integer.toString(value.getBrightness());
                break;
        }

    }

    public ArrayList<Integer> getIntegersFromTotalValue() {
        ArrayList<Integer> result = new ArrayList<>();
        String[] items = getTotalValue().split("|");

        for (int a = 0; a < items.length; a++) {
            try {
                result.add(Integer.valueOf(items[a]));
            } catch (NumberFormatException e) {
            }
        }

        return result;
    }

    public ArrayList<Integer> getRollerShutterPositions() {
        ArrayList<Integer> result = new ArrayList<>();
        String[] items = getTotalValue().split("\\|");

        for (int a = 0; a < items.length; a++) {
            String[] n = items[a].split(":");
            if (n.length == 2) {
                try {
                    int pos = Integer.valueOf(n[0]).intValue();
                    int sensor = Integer.valueOf(n[1]).intValue();

                    if (pos < 100 && sensor == 1) {
                        pos = 100;
                    }

                    result.add(Integer.valueOf(pos));
                } catch (NumberFormatException e) {
                }
            }


        }

        return result;
    }

    public ArrayList<Double> getDoubleValues() {

        ArrayList<Double> result = new ArrayList<>();
        String[] items = getTotalValue().split("\\|");

        for (int a = 0; a < items.length; a++) {

            try {
                result.add(Double.valueOf(items[a]));
            } catch (NumberFormatException e) {
            }

        }
        return result;
    }

    private ArrayList<Double> getRGBWValues(int idx) {

        ArrayList<Double> result = new ArrayList<>();
        String[] items = getTotalValue().split("\\|");

        for (int a = 0; a < items.length; a++) {
            String[] n = items[a].split(":");
            if (idx < n.length) {
                try {
                    result.add(Double.valueOf(n[idx]));
                } catch (NumberFormatException e) {
                }
            }
        }

        return result;
    }

    public ArrayList<Double> getColors() {
        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                return getRGBWValues(0);
        }
        return null;
    }

    public ArrayList<Double> getColorBrightness() {
        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                return getRGBWValues(1);
        }
        return null;
    }

    public ArrayList<Double> getBrightness() {
        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                return getDoubleValues();
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                return getRGBWValues(2);
        }
        return null;
    }

    public int getActivePercent(int idx) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] items = getTotalValue().split("\\|");

        int sum = 0;
        int count = 0;
        String[] n = null;

        for (int a = 0; a < items.length; a++) {

            switch (getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                    try {
                        sum += Integer.valueOf(items[a]).intValue() > 0 ? 1 : 0;
                    } catch (NumberFormatException e) {
                    }
                    count++;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    n = items[a].split(":");
                    if (n.length == 2) {
                        try {
                            sum += Integer.valueOf(n[1]).intValue() > 0 ? 1 : 0;
                        } catch (NumberFormatException e) {
                        }
                    }

                    count++;
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                    n = items[a].split(":");
                    if (n.length == 3) {
                        try {
                            if (idx == 0 || idx == 1) {
                                sum += Integer.valueOf(n[1]).intValue() > 0 ? 1 : 0;
                            }

                            if (idx == 0 || idx == 2) {
                                sum += Integer.valueOf(n[2]).intValue() > 0 ? 1 : 0;
                            }

                        } catch (NumberFormatException e) {
                        }
                    }

                    if (idx == 0) {
                        count += 2;
                    } else {
                        count++;
                    }

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                    n = items[a].split(":");
                    if (n.length == 2) {
                        try {
                            if (Integer.valueOf(n[0]).intValue() >= 100 // percent
                                    || Integer.valueOf(n[1]).intValue() > 0) { // sensor
                                sum++;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                    count++;
                    break;
            }


        }

        return count == 0 ? 0 : sum * 100 / count;
    }

    public int getActivePercent() {
        return getActivePercent(0);
    }

    public ImageId getImageIdx(WhichOne whichImage) {
        int active = 0;

        if (getFunc() == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING) {

            if (getActivePercent(2) >= 100) {
                active = 0x1;
            }

            if (getActivePercent(1) >= 100) {
                active |= 0x2;
            }

        } else {
            active = getActivePercent() >= 100 ? 1 : 0;
        }

        return super.getImageIdx(whichImage, active);
    }

    public CharSequence getHumanReadableValue(WhichOne whichOne) {
        return null;
    }

    public CharSequence getHumanReadableValue() {
        return null;
    }


}
