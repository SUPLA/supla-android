package org.supla.android.listview;
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
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.supla.android.R;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaConst;

import static org.supla.android.ChannelDetailThermostatHP.STATUS_POWERON;
import static org.supla.android.ChannelDetailThermostatHP.STATUS_PROGRAMMODE;

public class ThermostatHPListViewCursorAdapter extends ResourceCursorAdapter {
    public ThermostatHPListViewCursorAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c);
    }

    public ThermostatHPListViewCursorAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
        super(context, layout, c, autoRequery);
    }

    public ThermostatHPListViewCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    private boolean isOn(TextView tv) {
        return tv != null && ((Integer)tv.getTag()).intValue() == 1;
    }

    private void setOn(TextView tv, boolean on) {
        if (tv!=null) {
            if (on) {
                tv.setBackgroundResource( R.drawable.hp_button_on);
                tv.setTag(1);
            } else {
                tv.setBackgroundResource( R.drawable.hp_button_off);
                tv.setTag(0);
            }
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView caption = view.findViewById(R.id.hprCaption);
        SuplaChannelStatus status = view.findViewById(R.id.hprStatus);
        TextView onoff = view.findViewById(R.id.hprOnOff);
        TextView normal = view.findViewById(R.id.hprNormal);
        TextView eco = view.findViewById(R.id.hprEco);
        TextView auto = view.findViewById(R.id.hprAuto);
        TextView turbo = view.findViewById(R.id.hprTurbo);

        setOn(onoff, false);
        setOn(normal, false);
        setOn(eco, false);
        setOn(auto, false);
        setOn(turbo, false);

        status.setOnlineColor(context.getResources().getColor(R.color.channel_dot_on));
        status.setOfflineColor(context.getResources().getColor(R.color.channel_dot_off));
        status.setShapeType(SuplaChannelStatus.ShapeType.Dot);

        Channel channel = new Channel();
        channel.AssignCursorData(cursor);

        caption.setText(channel.getNotEmptyCaption(context));
        status.setPercent(channel.getOnLinePercent());

        ChannelExtendedValue cev = channel == null ? null : channel.getExtendedValue();
        if (cev == null
                || cev.getType() != SuplaConst.EV_TYPE_THERMOSTAT_DETAILS_V1
                || cev.getExtendedValue().ThermostatValue == null) {
            return;
        }


        Double temp = cev.getExtendedValue().ThermostatValue.getPresetTemperature(0);
        double presetTemperatureMin = temp != null ? temp.intValue() : 0;
        double measuredTemperatureMin =
                cev.getExtendedValue().ThermostatValue.getMeasuredTemperature(0);

        String tempTxt = String.valueOf(ChannelBase.getHumanReadableThermostatTemperature(
                measuredTemperatureMin, null,
                Double.valueOf(presetTemperatureMin),
                null, 1f, 1f));

        caption.setText(caption.getText() + " | " + tempTxt);

        Double ecoReduction = cev.getExtendedValue().ThermostatValue.getPresetTemperature(3);
        if (ecoReduction != null && ecoReduction > 0.0) {
            setOn(eco, true);
        }

        Integer flags = cev.getExtendedValue().ThermostatValue.getFlags(4);

        if (flags != null) {
            if ((flags & STATUS_POWERON) > 0) {
                setOn(onoff, true);
            }

            if ((flags & STATUS_PROGRAMMODE) > 0) {
                setOn(auto, true);
            }
        }

        Integer trb = cev.getExtendedValue().ThermostatValue.getValues(4);
        if (trb != null && trb > 0) {
            setOn(turbo, true);
        }

        if (isOn(onoff) && !isOn(eco)
                && !isOn(turbo) && !isOn(auto)) {
            setOn(normal, true);
        }

    }
}
