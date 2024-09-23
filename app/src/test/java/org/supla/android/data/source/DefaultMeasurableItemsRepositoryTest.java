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
import org.supla.android.data.source.local.ImpulseCounterLogDao;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.ThermostatMeasurementItem;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMeasurableItemsRepositoryTest {

  private static final double DELTA = 1e-15;

  @Mock private ImpulseCounterLogDao impulseCounterLogDao;
  @Mock private ThermostatLogDao thermostatLogDao;
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(impulseCounterLogDao);
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
    verifyNoInteractions(impulseCounterLogDao);
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
    verifyNoInteractions(impulseCounterLogDao);
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
    verifyNoInteractions(impulseCounterLogDao);
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
    verifyNoInteractions(impulseCounterLogDao);
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(thermostatLogDao);
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
    verifyNoInteractions(thermostatLogDao);
  }
}
