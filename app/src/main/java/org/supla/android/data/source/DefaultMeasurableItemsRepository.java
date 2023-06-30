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
import java.util.Date;
import org.supla.android.data.source.local.ElectricityMeterLogDao;
import org.supla.android.data.source.local.ImpulseCounterLogDao;
import org.supla.android.data.source.local.TempHumidityLogDao;
import org.supla.android.data.source.local.TemperatureLogDao;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.db.ElectricityMeasurementItem;
import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.TempHumidityMeasurementItem;
import org.supla.android.db.TemperatureMeasurementItem;
import org.supla.android.db.ThermostatMeasurementItem;

public class DefaultMeasurableItemsRepository implements MeasurableItemsRepository {

  private final ImpulseCounterLogDao impulseCounterLogDao;
  private final ElectricityMeterLogDao electricityMeterLogDao;
  private final ThermostatLogDao thermostatLogDao;
  private final TempHumidityLogDao tempHumidityLogDao;
  private final TemperatureLogDao temperatureLogDao;

  public DefaultMeasurableItemsRepository(
      ImpulseCounterLogDao impulseCounterLogDao,
      ElectricityMeterLogDao electricityMeterLogDao,
      ThermostatLogDao thermostatLogDao,
      TempHumidityLogDao tempHumidityLogDao,
      TemperatureLogDao temperatureLogDao) {
    this.impulseCounterLogDao = impulseCounterLogDao;
    this.electricityMeterLogDao = electricityMeterLogDao;
    this.thermostatLogDao = thermostatLogDao;
    this.tempHumidityLogDao = tempHumidityLogDao;
    this.temperatureLogDao = temperatureLogDao;
  }

  @Override
  public double getLastImpulseCounterMeasurementValue(int monthOffset, int channelId) {
    return impulseCounterLogDao.getLastImpulseCounterMeasurementValue(monthOffset, channelId);
  }

  @Override
  public double getLastElectricityMeterMeasurementValue(
      int monthOffset, int channelId, boolean production) {
    return electricityMeterLogDao.getLastElectricityMeterMeasurementValue(
        monthOffset, channelId, production);
  }

  @Override
  public int getElectricityMeterMeasurementTimestamp(int channelId, boolean min) {
    return electricityMeterLogDao.getElectricityMeterMeasurementTimestamp(channelId, min);
  }

  @Override
  public int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement) {
    return electricityMeterLogDao.getElectricityMeterMeasurementTotalCount(
        channelId, withoutComplement);
  }

  @Override
  public void addElectricityMeasurement(ElectricityMeasurementItem emi) {
    electricityMeterLogDao.addElectricityMeasurement(emi);
  }

  @Override
  public Cursor getElectricityMeasurementsCursor(
      int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
    return electricityMeterLogDao.getElectricityMeasurementsCursor(
        channelId, groupByDateFormat, dateFrom, dateTo);
  }

  @Override
  public void deleteElectricityMeasurements(int channelId) {
    electricityMeterLogDao.deleteElectricityMeasurements(channelId);
  }

  @Override
  public int getThermostatMeasurementTimestamp(int channelId, boolean min) {
    return thermostatLogDao.getThermostatMeasurementTimestamp(channelId, min);
  }

  @Override
  public int getThermostatMeasurementTotalCount(int channelId) {
    return thermostatLogDao.getThermostatMeasurementTotalCount(channelId);
  }

  @Override
  public void deleteThermostatMeasurements(int channelId) {
    thermostatLogDao.deleteThermostatMeasurements(channelId);
  }

  @Override
  public void addThermostatMeasurement(ThermostatMeasurementItem emi) {
    thermostatLogDao.addThermostatMeasurement(emi);
  }

  @Override
  public Cursor getThermostatMeasurements(int channelId) {
    return thermostatLogDao.getThermostatMeasurements(channelId);
  }

  @Override
  public int getTempHumidityMeasurementTimestamp(int channelId, boolean min) {
    return tempHumidityLogDao.getTempHumidityMeasurementTimestamp(channelId, min);
  }

  @Override
  public int getTempHumidityMeasurementTotalCount(int channelId) {
    return tempHumidityLogDao.getTempHumidityMeasurementTotalCount(channelId);
  }

  @Override
  public void deleteTempHumidityMeasurements(int channelId) {
    tempHumidityLogDao.deleteTempHumidityMeasurements(channelId);
  }

  @Override
  public void addTempHumidityMeasurement(TempHumidityMeasurementItem emi) {
    tempHumidityLogDao.addTempHumidityMeasurement(emi);
  }

  @Override
  public Cursor getTempHumidityMeasurements(int channelId, Date dateFrom, Date dateTo) {
    return tempHumidityLogDao.getTempHumidityMeasurements(channelId, dateFrom, dateTo);
  }

  @Override
  public int getTemperatureMeasurementTimestamp(int channelId, boolean min) {
    return temperatureLogDao.getTemperatureMeasurementTimestamp(channelId, min);
  }

  @Override
  public int getTemperatureMeasurementTotalCount(int channelId) {
    return temperatureLogDao.getTemperatureMeasurementTotalCount(channelId);
  }

  @Override
  public void deleteTemperatureMeasurements(int channelId) {
    temperatureLogDao.deleteTemperatureMeasurements(channelId);
  }

  @Override
  public void addTemperatureMeasurement(TemperatureMeasurementItem emi) {
    temperatureLogDao.addTemperatureMeasurement(emi);
  }

  @Override
  public Cursor getTemperatureMeasurements(int channelId, Date dateFrom, Date dateTo) {
    return temperatureLogDao.getTemperatureMeasurements(channelId, dateFrom, dateTo);
  }

  @Override
  public void addImpulseCounterMeasurement(ImpulseCounterMeasurementItem item) {
    impulseCounterLogDao.addImpulseCounterMeasurement(item);
  }

  @Override
  public int getImpulseCounterMeasurementTimestamp(int channelId, boolean min) {
    return impulseCounterLogDao.getImpulseCounterMeasurementTimestamp(channelId, min);
  }

  @Override
  public boolean impulseCounterMeasurementsStartsWithTheCurrentMonth(int channelId) {
    return impulseCounterLogDao.impulseCounterMeasurementsStartsWithTheCurrentMonth(channelId);
  }

  @Override
  public int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement) {
    return impulseCounterLogDao.getImpulseCounterMeasurementTotalCount(
        channelId, withoutComplement);
  }

  @Override
  public Cursor getImpulseCounterMeasurements(
      int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
    return impulseCounterLogDao.getImpulseCounterMeasurements(
        channelId, groupByDateFormat, dateFrom, dateTo);
  }

  @Override
  public void deleteImpulseCounterMeasurements(int channelId) {
    impulseCounterLogDao.deleteImpulseCounterMeasurements(channelId);
  }
}
