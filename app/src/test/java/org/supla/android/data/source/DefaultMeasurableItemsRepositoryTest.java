package org.supla.android.data.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public class DefaultMeasurableItemsRepositoryTest {

  private static final double DELTA = 1e-15;

  @Mock private ImpulseCounterLogDao impulseCounterLogDao;
  @Mock private ElectricityMeterLogDao electricityMeterLogDao;
  @Mock private ThermostatLogDao thermostatLogDao;
  @Mock private TempHumidityLogDao tempHumidityLogDao;
  @Mock private TemperatureLogDao temperatureLogDao;
  @InjectMocks private DefaultMeasurableItemsRepository measurableItemsRepository;

  @Test
  public void shouldGetLastImpulseCounterMeasurementValue() {
    // given
    int monthOffset = 1;
    int channelId = 2;
    double expected = 4.5;
    when(impulseCounterLogDao.getLastImpulseCounterMeasurementValue(monthOffset, channelId))
        .thenReturn(expected);

    // when
    double result =
        measurableItemsRepository.getLastImpulseCounterMeasurementValue(monthOffset, channelId);

    // then
    assertEquals(expected, result, DELTA);

    verify(impulseCounterLogDao).getLastImpulseCounterMeasurementValue(monthOffset, channelId);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetLastElectricityMeterMeasurementValue() {
    // given
    int monthOffset = 1;
    int channelId = 2;
    boolean production = true;
    double expected = 4.5;
    when(electricityMeterLogDao.getLastElectricityMeterMeasurementValue(
            monthOffset, channelId, production))
        .thenReturn(expected);

    // when
    double result =
        measurableItemsRepository.getLastElectricityMeterMeasurementValue(
            monthOffset, channelId, production);

    // then
    assertEquals(expected, result, DELTA);

    verify(electricityMeterLogDao)
        .getLastElectricityMeterMeasurementValue(monthOffset, channelId, production);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetElectricityMeterMeasurementTimestamp() {
    // given
    int channelId = 2;
    boolean min = true;
    int expected = 4;
    when(electricityMeterLogDao.getElectricityMeterMeasurementTimestamp(channelId, min))
        .thenReturn(expected);

    // when
    int result = measurableItemsRepository.getElectricityMeterMeasurementTimestamp(channelId, min);

    // then
    assertEquals(expected, result);

    verify(electricityMeterLogDao).getElectricityMeterMeasurementTimestamp(channelId, min);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetElectricityMeterMeasurementTotalCount() {
    // given
    int channelId = 2;
    boolean withoutComplement = true;
    int expected = 4;
    when(electricityMeterLogDao.getElectricityMeterMeasurementTotalCount(
            channelId, withoutComplement))
        .thenReturn(expected);

    // when
    int result =
        measurableItemsRepository.getElectricityMeterMeasurementTotalCount(
            channelId, withoutComplement);

    // then
    assertEquals(expected, result);

    verify(electricityMeterLogDao)
        .getElectricityMeterMeasurementTotalCount(channelId, withoutComplement);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldAddElectricityMeasurement() {
    // given
    ElectricityMeasurementItem item = mock(ElectricityMeasurementItem.class);

    // when
    measurableItemsRepository.addElectricityMeasurement(item);

    // then
    verify(electricityMeterLogDao).addElectricityMeasurement(item);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetElectricityMeasurementsCursor() {
    // given
    int channelId = 2;
    String groupByDateFormat = "format";
    Date dateFrom = mock(Date.class);
    Date dateTo = mock(Date.class);
    Cursor expected = mock(Cursor.class);
    when(electricityMeterLogDao.getElectricityMeasurementsCursor(
            channelId, groupByDateFormat, dateFrom, dateTo))
        .thenReturn(expected);

    // when
    Cursor result =
        measurableItemsRepository.getElectricityMeasurementsCursor(
            channelId, groupByDateFormat, dateFrom, dateTo);

    // then
    assertSame(expected, result);

    verify(electricityMeterLogDao)
        .getElectricityMeasurementsCursor(channelId, groupByDateFormat, dateFrom, dateTo);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldDeleteElectricityMeasurements() {
    // given
    int channelId = 1;

    // when
    measurableItemsRepository.deleteElectricityMeasurements(channelId);

    // then
    verify(electricityMeterLogDao).deleteElectricityMeasurements(channelId);
    verifyNoMoreInteractions(electricityMeterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetThermostatMeasurementTimestamp() {
    // given
    int channelId = 2;
    boolean min = true;
    int expected = 4;
    when(thermostatLogDao.getThermostatMeasurementTimestamp(channelId, min)).thenReturn(expected);

    // when
    int result = measurableItemsRepository.getThermostatMeasurementTimestamp(channelId, min);

    // then
    assertEquals(expected, result);

    verify(thermostatLogDao).getThermostatMeasurementTimestamp(channelId, min);
    verifyNoMoreInteractions(thermostatLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetThermostatMeasurementTotalCount() {
    // given
    int channelId = 2;
    int expected = 4;
    when(thermostatLogDao.getThermostatMeasurementTotalCount(channelId)).thenReturn(expected);

    // when
    int result = measurableItemsRepository.getThermostatMeasurementTotalCount(channelId);

    // then
    assertEquals(expected, result);

    verify(thermostatLogDao).getThermostatMeasurementTotalCount(channelId);
    verifyNoMoreInteractions(thermostatLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldDeleteThermostatMeasurements() {
    // given
    int channelId = 1;

    // when
    measurableItemsRepository.deleteThermostatMeasurements(channelId);

    // then
    verify(thermostatLogDao).deleteThermostatMeasurements(channelId);
    verifyNoMoreInteractions(thermostatLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldAddThermostatMeasurement() {
    // given
    ThermostatMeasurementItem item = mock(ThermostatMeasurementItem.class);

    // when
    measurableItemsRepository.addThermostatMeasurement(item);

    // then
    verify(thermostatLogDao).addThermostatMeasurement(item);
    verifyNoMoreInteractions(thermostatLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetThermostatMeasurementsCursor() {
    // given
    int channelId = 2;
    Date startDate = mock(Date.class);
    Date endDate = mock(Date.class);
    Cursor expected = mock(Cursor.class);
    when(thermostatLogDao.getThermostatMeasurements(channelId, startDate, endDate))
        .thenReturn(expected);

    // when
    Cursor result =
        measurableItemsRepository.getThermostatMeasurements(channelId, startDate, endDate);

    // then
    assertSame(expected, result);

    verify(thermostatLogDao).getThermostatMeasurements(channelId, startDate, endDate);
    verifyNoMoreInteractions(thermostatLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetTempHumidityMeasurementTimestamp() {
    // given
    int channelId = 2;
    boolean min = true;
    int expected = 4;
    when(tempHumidityLogDao.getTempHumidityMeasurementTimestamp(channelId, min))
        .thenReturn(expected);

    // when
    int result = measurableItemsRepository.getTempHumidityMeasurementTimestamp(channelId, min);

    // then
    assertEquals(expected, result);

    verify(tempHumidityLogDao).getTempHumidityMeasurementTimestamp(channelId, min);
    verifyNoMoreInteractions(tempHumidityLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetTempHumidityMeasurementTotalCount() {
    // given
    int channelId = 2;
    int expected = 4;
    when(tempHumidityLogDao.getTempHumidityMeasurementTotalCount(channelId)).thenReturn(expected);

    // when
    int result = measurableItemsRepository.getTempHumidityMeasurementTotalCount(channelId);

    // then
    assertEquals(expected, result);

    verify(tempHumidityLogDao).getTempHumidityMeasurementTotalCount(channelId);
    verifyNoMoreInteractions(tempHumidityLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, temperatureLogDao);
  }

  @Test
  public void shouldDeleteTempHumidityMeasurements() {
    // given
    int channelId = 1;

    // when
    measurableItemsRepository.deleteTempHumidityMeasurements(channelId);

    // then
    verify(tempHumidityLogDao).deleteTempHumidityMeasurements(channelId);
    verifyNoMoreInteractions(tempHumidityLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, temperatureLogDao);
  }

  @Test
  public void shouldAddTempHumidityMeasurement() {
    // given
    TempHumidityMeasurementItem item = mock(TempHumidityMeasurementItem.class);

    // when
    measurableItemsRepository.addTempHumidityMeasurement(item);

    // then
    verify(tempHumidityLogDao).addTempHumidityMeasurement(item);
    verifyNoMoreInteractions(tempHumidityLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetTempHumidityMeasurementsCursor() {
    // given
    int channelId = 2;
    Date dateFrom = mock(Date.class);
    Date dateTo = mock(Date.class);
    Cursor expected = mock(Cursor.class);
    when(tempHumidityLogDao.getTempHumidityMeasurements(channelId, dateFrom, dateTo))
        .thenReturn(expected);

    // when
    Cursor result =
        measurableItemsRepository.getTempHumidityMeasurements(channelId, dateFrom, dateTo);

    // then
    assertSame(expected, result);

    verify(tempHumidityLogDao).getTempHumidityMeasurements(channelId, dateFrom, dateTo);
    verifyNoMoreInteractions(tempHumidityLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, temperatureLogDao);
  }

  @Test
  public void shouldGetTemperatureMeasurementTimestamp() {
    // given
    int channelId = 2;
    boolean min = true;
    int expected = 4;
    when(temperatureLogDao.getTemperatureMeasurementTimestamp(channelId, min)).thenReturn(expected);

    // when
    int result = measurableItemsRepository.getTemperatureMeasurementTimestamp(channelId, min);

    // then
    assertEquals(expected, result);

    verify(temperatureLogDao).getTemperatureMeasurementTimestamp(channelId, min);
    verifyNoMoreInteractions(temperatureLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetTemperatureMeasurementTotalCount() {
    // given
    int channelId = 2;
    int expected = 4;
    when(temperatureLogDao.getTemperatureMeasurementTotalCount(channelId)).thenReturn(expected);

    // when
    int result = measurableItemsRepository.getTemperatureMeasurementTotalCount(channelId);

    // then
    assertEquals(expected, result);

    verify(temperatureLogDao).getTemperatureMeasurementTotalCount(channelId);
    verifyNoMoreInteractions(temperatureLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldDeleteTemperatureMeasurements() {
    // given
    int channelId = 1;

    // when
    measurableItemsRepository.deleteTemperatureMeasurements(channelId);

    // then
    verify(temperatureLogDao).deleteTemperatureMeasurements(channelId);
    verifyNoMoreInteractions(temperatureLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldAddTemperatureMeasurement() {
    // given
    TemperatureMeasurementItem item = mock(TemperatureMeasurementItem.class);

    // when
    measurableItemsRepository.addTemperatureMeasurement(item);

    // then
    verify(temperatureLogDao).addTemperatureMeasurement(item);
    verifyNoMoreInteractions(temperatureLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetTemperatureMeasurementsCursor() {
    // given
    int channelId = 2;
    Date dateFrom = mock(Date.class);
    Date dateTo = mock(Date.class);
    Cursor expected = mock(Cursor.class);
    when(temperatureLogDao.getTemperatureMeasurements(channelId, dateFrom, dateTo))
        .thenReturn(expected);

    // when
    Cursor result =
        measurableItemsRepository.getTemperatureMeasurements(channelId, dateFrom, dateTo);

    // then
    assertSame(expected, result);

    verify(temperatureLogDao).getTemperatureMeasurements(channelId, dateFrom, dateTo);
    verifyNoMoreInteractions(temperatureLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldAddImpulseCounterMeasurement() {
    // given
    ImpulseCounterMeasurementItem item = mock(ImpulseCounterMeasurementItem.class);

    // when
    measurableItemsRepository.addImpulseCounterMeasurement(item);

    // then
    verify(impulseCounterLogDao).addImpulseCounterMeasurement(item);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        temperatureLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetImpulseCounterMeasurementTimestamp() {
    // given
    int channelId = 2;
    boolean min = true;
    int expected = 4;
    when(impulseCounterLogDao.getImpulseCounterMeasurementTimestamp(channelId, min))
        .thenReturn(expected);

    // when
    int result = measurableItemsRepository.getImpulseCounterMeasurementTimestamp(channelId, min);

    // then
    assertEquals(expected, result);

    verify(impulseCounterLogDao).getImpulseCounterMeasurementTimestamp(channelId, min);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        temperatureLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetImpulseCounterMeasurementsStartsWithTheCurrentMonth() {
    // given
    int channelId = 2;
    boolean expected = true;
    when(impulseCounterLogDao.impulseCounterMeasurementsStartsWithTheCurrentMonth(channelId))
        .thenReturn(expected);

    // when
    boolean result =
        measurableItemsRepository.impulseCounterMeasurementsStartsWithTheCurrentMonth(channelId);

    // then
    assertEquals(expected, result);

    verify(impulseCounterLogDao).impulseCounterMeasurementsStartsWithTheCurrentMonth(channelId);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        temperatureLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetImpulseCounterMeasurementTotalCount() {
    // given
    int channelId = 2;
    boolean withoutComplement = true;
    int expected = 4;
    when(impulseCounterLogDao.getImpulseCounterMeasurementTotalCount(channelId, withoutComplement))
        .thenReturn(expected);

    // when
    int result =
        measurableItemsRepository.getImpulseCounterMeasurementTotalCount(
            channelId, withoutComplement);

    // then
    assertEquals(expected, result);

    verify(impulseCounterLogDao)
        .getImpulseCounterMeasurementTotalCount(channelId, withoutComplement);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        impulseCounterLogDao, electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao);
  }

  @Test
  public void shouldGetImpulseCounterMeasurementsCursor() {
    // given
    int channelId = 2;
    String groupByDateFormat = "format";
    Date dateFrom = mock(Date.class);
    Date dateTo = mock(Date.class);
    Cursor expected = mock(Cursor.class);
    when(impulseCounterLogDao.getImpulseCounterMeasurements(
            channelId, groupByDateFormat, dateFrom, dateTo))
        .thenReturn(expected);

    // when
    Cursor result =
        measurableItemsRepository.getImpulseCounterMeasurements(
            channelId, groupByDateFormat, dateFrom, dateTo);

    // then
    assertSame(expected, result);

    verify(impulseCounterLogDao)
        .getImpulseCounterMeasurements(channelId, groupByDateFormat, dateFrom, dateTo);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }

  @Test
  public void shouldDeleteImpulseCounterMeasurements() {
    // given
    int channelId = 1;

    // when
    measurableItemsRepository.deleteImpulseCounterMeasurements(channelId);

    // then
    verify(impulseCounterLogDao).deleteImpulseCounterMeasurements(channelId);
    verifyNoMoreInteractions(impulseCounterLogDao);
    verifyNoInteractions(
        electricityMeterLogDao, thermostatLogDao, tempHumidityLogDao, temperatureLogDao);
  }
}
