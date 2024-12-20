package org.supla.android.data.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.db.ThermostatMeasurementItem;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMeasurableItemsRepositoryTest {

  @Mock private ThermostatLogDao thermostatLogDao;
  @InjectMocks private DefaultMeasurableItemsRepository measurableItemsRepository;

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
  }
}
