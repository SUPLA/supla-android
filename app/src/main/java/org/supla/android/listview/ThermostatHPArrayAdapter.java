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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import org.supla.android.R;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.ThermostatHP;
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity;
import org.supla.android.db.Channel;

public class ThermostatHPArrayAdapter extends ArrayAdapter<ChannelDataEntity> {

  private final LayoutInflater inflater;

  public ThermostatHPArrayAdapter(Context context, int layout, List<ChannelDataEntity> entries) {
    super(context, layout, entries);
    inflater = LayoutInflater.from(context);
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

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view;
    if (convertView == null) {
      view = inflater.inflate(R.layout.homeplus_channel_row, parent, false);
    } else {
      view = convertView;
    }

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

    status.setOnlineColor(getContext().getResources().getColor(R.color.primary));
    status.setOfflineColor(getContext().getResources().getColor(R.color.red));
    status.setShapeType(SuplaChannelStatus.ShapeType.Dot);

    Channel channel = getItem(position).getLegacyChannel();

    caption.setText(channel.getCaption(getContext()));
    status.setPercent(channel.getOnLinePercent());

    ThermostatHP thermostat = new ThermostatHP();
    if (!thermostat.assign(channel)) {
      return view;
    }

    String tempTxt =
        String.valueOf(
            channel.getHumanReadableThermostatTemperature(
                thermostat.getMeasuredTemperatureMin(),
                null,
                (double) thermostat.getPresetTemperatureMin(),
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

    return view;
  }
}
