package org.supla.android.data.source
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

import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.dao.measurements.TemperatureAndHumidityLogDao
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.extensions.date

@RunWith(MockitoJUnitRunner::class)
class TemperatureAndHumidityLogRepositoryTest {

  @Mock
  private lateinit var temperatureAndHumidityLogDao: TemperatureAndHumidityLogDao

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @InjectMocks
  private lateinit var repository: TemperatureAndHumidityLogRepository

  @Test
  fun `should find measurements in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val startDate = date(2023, 1, 1)
    val endDate = date(2023, 2, 1)
    val entity: TemperatureAndHumidityLogEntity = mockk()
    whenever(temperatureAndHumidityLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time))
      .thenReturn(Observable.just(listOf(entity)))

    // when
    val testObserver = repository.findMeasurements(remoteId, profileId, startDate, endDate).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(listOf(entity))

    verify(temperatureAndHumidityLogDao).findMeasurements(remoteId, profileId, startDate.time, endDate.time)
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should delete measurements from DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    whenever(temperatureAndHumidityLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val testObserver = repository.delete(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(temperatureAndHumidityLogDao).delete(remoteId, profileId)
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find count in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val count = 123
    whenever(temperatureAndHumidityLogDao.findCount(remoteId, profileId))
      .thenReturn(Maybe.just(count))

    // when
    val testObserver = repository.findCount(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(count)

    verify(temperatureAndHumidityLogDao).findCount(remoteId, profileId)
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find min timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(temperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMinTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(temperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find max timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(temperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMaxTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(temperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should insert to DB`() {
    // given
    val entity: TemperatureAndHumidityLogEntity = mockk()
    whenever(temperatureAndHumidityLogDao.insert(listOf(entity)))
      .thenReturn(Completable.complete())

    // when
    val testObserver = repository.insert(listOf(entity)).test()

    // then
    testObserver.assertComplete()

    verify(temperatureAndHumidityLogDao).insert(listOf(entity))
    verifyNoMoreInteractions(temperatureAndHumidityLogDao)
    verifyNoInteractions(suplaCloudServiceProvider)
  }
}
