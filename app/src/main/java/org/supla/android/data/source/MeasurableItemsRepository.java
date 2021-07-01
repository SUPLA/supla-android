package org.supla.android.data.source;

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

import android.database.Cursor;

import org.supla.android.db.ElectricityMeasurementItem;
import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.TempHumidityMeasurementItem;
import org.supla.android.db.TemperatureMeasurementItem;
import org.supla.android.db.ThermostatMeasurementItem;

import java.util.Date;

public interface MeasurableItemsRepository {

    double getLastImpulseCounterMeasurementValue(int monthOffset, int channelId);

    double getLastElectricityMeterMeasurementValue(int monthOffset, int channelId, boolean production);

    int getElectricityMeterMeasurementTimestamp(int channelId, boolean min);

    int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement);

    void addElectricityMeasurement(ElectricityMeasurementItem emi);

    Cursor getElectricityMeasurementsCursor(int channelId, String groupByDateFormat, Date dateFrom, Date dateTo);

    void deleteElectricityMeasurements(int channelId);

    int getThermostatMeasurementTimestamp(int channelId, boolean min);

    int getThermostatMeasurementTotalCount(int channelId);

    void deleteThermostatMeasurements(int channelId);

    void addThermostatMeasurement(ThermostatMeasurementItem emi);

    Cursor getThermostatMeasurements(int channelId);

    int getTempHumidityMeasurementTimestamp(int channelId, boolean min);

    int getTempHumidityMeasurementTotalCount(int channelId);

    void deleteTempHumidityMeasurements(int channelId);

    void addTempHumidityMeasurement(TempHumidityMeasurementItem emi);

    Cursor getTempHumidityMeasurements(int channelId, Date dateFrom, Date dateTo);

    int getTemperatureMeasurementTimestamp(int channelId, boolean min);

    int getTemperatureMeasurementTotalCount(int channelId);

    void deleteTemperatureMeasurements(int channelId);

    void addTemperatureMeasurement(TemperatureMeasurementItem emi);

    Cursor getTemperatureMeasurements(int channelId, Date dateFrom, Date dateTo);

    void addImpulseCounterMeasurement(ImpulseCounterMeasurementItem item);

    int getImpulseCounterMeasurementTimestamp(int channelId, boolean min);

    boolean impulseCounterMeasurementsStartsWithTheCurrentMonth(int channelId);

    int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement);

    Cursor getImpulseCounterMeasurements(int channelId, String groupByDateFormat, Date dateFrom, Date dateTo);

    void deleteImpulseCounterMeasurements(int channelId);
}
