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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.local.dao.measurements.TemperatureAndHumidityLogDao
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.extensions.date

class TemperatureAndHumidityLogRepositoryTest {

  @MockK
  private lateinit var temperatureAndHumidityLogDao: TemperatureAndHumidityLogDao

  @MockK
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @InjectMockKs
  private lateinit var repository: TemperatureAndHumidityLogRepository

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should find measurements in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val startDate = date(2023, 1, 1)
    val endDate = date(2023, 2, 1)
    val entity: TemperatureAndHumidityLogEntity = mockk()
    every {
      temperatureAndHumidityLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)
    } returns Observable.just(listOf(entity))

    // when
    val testObserver = repository.findMeasurements(remoteId, profileId, startDate, endDate).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(listOf(entity))

    verify {
      temperatureAndHumidityLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }

  @Test
  fun `should delete measurements from DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    every { temperatureAndHumidityLogDao.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val testObserver = repository.delete(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      temperatureAndHumidityLogDao.delete(remoteId, profileId)
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }

  @Test
  fun `should find count in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val count = 123
    every { temperatureAndHumidityLogDao.findCount(remoteId, profileId) } returns Maybe.just(count)

    // when
    val testObserver = repository.findCount(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(count)

    verify {
      temperatureAndHumidityLogDao.findCount(remoteId, profileId)
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }

  @Test
  fun `should find min timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    every { temperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId) } returns Single.just(timestamp)

    // when
    val testObserver = repository.findMinTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify {
      temperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId)
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }

  @Test
  fun `should find max timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    every { temperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId) } returns Single.just(timestamp)

    // when
    val testObserver = repository.findMaxTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify {
      temperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId)
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }

  @Test
  fun `should insert to DB`() {
    // given
    val entity: TemperatureAndHumidityLogEntity = mockk()
    every { temperatureAndHumidityLogDao.insert(listOf(entity)) } returns Completable.complete()

    // when
    val testObserver = repository.insert(listOf(entity)).test()

    // then
    testObserver.assertComplete()

    verify {
      temperatureAndHumidityLogDao.insert(listOf(entity))
    }
    confirmVerified(temperatureAndHumidityLogDao, suplaCloudServiceProvider)
  }
}
