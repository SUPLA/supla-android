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
import org.supla.android.data.source.local.ImpulseCounterLogDao;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.ThermostatMeasurementItem;

public class DefaultMeasurableItemsRepository implements MeasurableItemsRepository {

  private final ImpulseCounterLogDao impulseCounterLogDao;
  private final ThermostatLogDao thermostatLogDao;

  public DefaultMeasurableItemsRepository(
      ImpulseCounterLogDao impulseCounterLogDao, ThermostatLogDao thermostatLogDao) {
    this.impulseCounterLogDao = impulseCounterLogDao;
    this.thermostatLogDao = thermostatLogDao;
  }

  @Override
  public double getLastImpulseCounterMeasurementValue(int monthOffset, int channelId) {
    return impulseCounterLogDao.getLastImpulseCounterMeasurementValue(monthOffset, channelId);
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
  public Cursor getThermostatMeasurements(int channelId, Date dateFrom, Date dateTo) {
    return thermostatLogDao.getThermostatMeasurements(channelId, dateFrom, dateTo);
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
