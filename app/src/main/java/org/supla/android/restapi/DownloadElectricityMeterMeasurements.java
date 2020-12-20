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

import org.supla.android.db.ElectricityMeasurementItem;
import org.supla.android.db.IncrementalMeasurementItem;


public class DownloadElectricityMeterMeasurements extends DownloadIncrementalMeasurements {

    public DownloadElectricityMeterMeasurements(Context context) {
        super(context);
    }

    protected long getMinTimestamp() {
        return getMeasurementsDbH().getElectricityMeterMeasurementTimestamp(getChannelId(), true);
    }

    protected long getMaxTimestamp() {
        return getMeasurementsDbH().getElectricityMeterMeasurementTimestamp(getChannelId(), false);
    }

    @Override
    protected int getLocalTotalCount() {
        return getMeasurementsDbH().getElectricityMeterMeasurementTotalCount(getChannelId(),
                true);
    }

    protected void eraseMeasurements() {
        getMeasurementsDbH().deleteElectricityMeasurements(getChannelId());
    }

    protected IncrementalMeasurementItem newObject() {
        return new ElectricityMeasurementItem();
    }

    protected IncrementalMeasurementItem newObject(IncrementalMeasurementItem src) {
        return new ElectricityMeasurementItem((ElectricityMeasurementItem) src);
    }

    protected IncrementalMeasurementItem getOlderUncalculatedIncrementalMeasurement(
            SQLiteDatabase db, int channelId, long timestamp) {
        return getMeasurementsDbH().getOlderUncalculatedElectricityMeasurement(
                channelId,
                timestamp);
    }

    protected void deleteUncalculatedIncrementalMeasurements(SQLiteDatabase db,
                                                             int channelId) {
        getMeasurementsDbH().deleteUncalculatedElectricityMeasurements(
                channelId);
    }

    protected void addIncrementalMeasurement(SQLiteDatabase db,
                                             IncrementalMeasurementItem item) {
        getMeasurementsDbH().addElectricityMeasurement((ElectricityMeasurementItem) item);
    }

}
