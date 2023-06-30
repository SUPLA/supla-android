package org.supla.android.restapi;

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
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.db.TemperatureMeasurementItem;

public class DownloadTemperatureMeasurements extends DownloadMeasurementLogs {

  public DownloadTemperatureMeasurements(Context context, int profileId) {
    super(context, profileId);
  }

  protected long getMinTimestamp() {
    return getMeasurementsDbH().getTemperatureMeasurementTimestamp(getChannelId(), true);
  }

  protected long getMaxTimestamp() {
    return getMeasurementsDbH().getTemperatureMeasurementTimestamp(getChannelId(), false);
  }

  @Override
  protected int getLocalTotalCount() {
    return getMeasurementsDbH().getTemperatureMeasurementTotalCount(getChannelId());
  }

  protected void eraseMeasurements() {
    getMeasurementsDbH().deleteTemperatureMeasurements(getChannelId());
  }

  protected void SaveMeasurementItem(SQLiteDatabase db, long timestamp, JSONObject obj)
      throws JSONException {

    TemperatureMeasurementItem ti = new TemperatureMeasurementItem();
    ti.AssignJSONObject(obj);
    ti.setChannelId(getChannelId());
    ti.setProfileId(getCurrentProfileId());

    getMeasurementsDbH().addTemperatureMeasurement(ti);
  }
}
