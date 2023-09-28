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
import org.supla.android.ThermostatHP;
import org.supla.android.db.Channel;

public class ThermostatHPListViewCursorAdapter extends ResourceCursorAdapter {
  public ThermostatHPListViewCursorAdapter(Context context, int layout, Cursor c) {
    super(context, layout, c);
  }

  public ThermostatHPListViewCursorAdapter(
      Context context, int layout, Cursor c, boolean autoRequery) {
    super(context, layout, c, autoRequery);
  }

  public ThermostatHPListViewCursorAdapter(Context context, int layout, Cursor c, int flags) {
    super(context, layout, c, flags);
  }

  private boolean isOn(TextView tv) {
    return tv != null && ((Integer) tv.getTag()).intValue() == 1;
  }

  private void setOn(TextView tv, boolean on) {
    if (tv != null) {
      if (on) {
        tv.setBackgroundResource(R.drawable.hp_button_on);
        tv.setTag(1);
      } else {
        tv.setBackgroundResource(R.drawable.hp_button_off);
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

    status.setOnlineColor(context.getResources().getColor(R.color.primary));
    status.setOfflineColor(context.getResources().getColor(R.color.red));
    status.setShapeType(SuplaChannelStatus.ShapeType.Dot);

    Channel channel = new Channel();
    channel.AssignCursorData(cursor);

    caption.setText(channel.getNotEmptyCaption(context));
    status.setPercent(channel.getOnLinePercent());

    ThermostatHP thermostat = new ThermostatHP();
    if (!thermostat.assign(channel)) {
      return;
    }

    String tempTxt =
        String.valueOf(
            channel.getHumanReadableThermostatTemperature(
                thermostat.getMeasuredTemperatureMin(),
                null,
                Double.valueOf(thermostat.getPresetTemperatureMin()),
                null,
                1f,
                1f));

    if (channel.getOnLine()) {
      caption.setText(caption.getText() + " | " + tempTxt);
    }

    setOn(eco, thermostat.isEcoRecuctionApplied());
    setOn(onoff, thermostat.isThermostatOn());
    setOn(auto, thermostat.isAutoOn());
    setOn(turbo, thermostat.isTurboOn());
    setOn(normal, thermostat.isNormalOn());
  }
}
