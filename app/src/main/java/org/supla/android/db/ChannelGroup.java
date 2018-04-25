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
        return 100;
    }

    public int getGroupId() {
        return getRemoteId();
    }

    public String getTotalValue() {
        return TotalValue;
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

        if (!BufferTotalValue.isEmpty()) {
            BufferTotalValue += "|";
        }

        switch (getFunc()) {
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                BufferTotalValue += Integer.toString(value.getSubValueHi() ? 1 : 0);
                break;
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                BufferTotalValue += Integer.toString(value.hiValue() ? 1 : 0);
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                BufferTotalValue += Integer.toString(value.getPercent());
                BufferTotalValue += ":";
                BufferTotalValue += Integer.toString(value.getSubValueHi() ? 1 : 0);
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

    public int getImageIdx(WhichOne whichImage) {
        return super.getImageIdx(whichImage, null);
    }

    public int getImageIdx() {
        return super.getImageIdx(WhichOne.First, null);
    }

    public String getHumanReadableValue(WhichOne whichOne) {
        return null;
    }

    public String getHumanReadableValue() {
        return null;
    }


}
