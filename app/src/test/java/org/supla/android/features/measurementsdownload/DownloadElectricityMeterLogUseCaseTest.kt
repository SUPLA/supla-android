package org.supla.android.features.measurementsdownload
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

import androidx.room.rxjava3.EmptyResultSetException
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.Headers
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.mock
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ElectricityMeasurement
import org.supla.android.data.source.remote.rest.channel.mock
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Calendar
import java.util.Date

class DownloadElectricityMeterLogUseCaseTest {

  @MockK
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @MockK
  private lateinit var electricityMeterLogRepository: ElectricityMeterLogRepository

  @InjectMockKs
  private lateinit var useCase: DownloadElectricityMeterLogUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert values calculated from consecutive electricity measurements when database is empty`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()
    val firstMeasurementDate = date(2023, Calendar.NOVEMBER, 1, 0, 0)
    val secondMeasurementDate = date(2023, Calendar.NOVEMBER, 1, 0, 10)
    val thirdMeasurementDate = date(2023, Calendar.NOVEMBER, 1, 0, 20)
    val fourthMeasurementDate = date(2023, Calendar.NOVEMBER, 1, 0, 30)

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(
      remoteId = remoteId,
      cloudService = cloudService,
      firstMeasurementDate = firstMeasurementDate,
      totalCount = 4
    )
    every { electricityMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.error(EmptyResultSetException(""))
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { electricityMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(0)
    every { electricityMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    every { electricityMeterLogRepository.insert(any()) } returns Completable.complete()
    every { electricityMeterLogRepository.getMeasurements(cloudService, remoteId, 0) } returns
      Observable.just(
        listOf(
          ElectricityMeasurement.mock(date = firstMeasurementDate, phase1Fae = 1000),
          ElectricityMeasurement.mock(date = secondMeasurementDate, phase1Fae = 1200),
          ElectricityMeasurement.mock(date = thirdMeasurementDate, phase1Fae = 100),
          ElectricityMeasurement.mock(date = fourthMeasurementDate, phase1Fae = 250)
        )
      )
    every { electricityMeterLogRepository.getMeasurements(cloudService, remoteId, fourthMeasurementDate.toTimestamp()) } returns
      Observable.just(emptyList())

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<ElectricityMeterLogEntity>>()
    verify {
      suplaCloudServiceProvider.provide()
      electricityMeterLogRepository.findMinTimestamp(remoteId, profileId)
      electricityMeterLogRepository.findOldestEntity(remoteId, profileId)
      electricityMeterLogRepository.findCount(remoteId, profileId)
      electricityMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
      electricityMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      electricityMeterLogRepository.getMeasurements(cloudService, remoteId, 0)
      electricityMeterLogRepository.getMeasurements(cloudService, remoteId, fourthMeasurementDate.toTimestamp())
      electricityMeterLogRepository.insert(capture(captor))
    }

    val result = captor.captured
    Assertions.assertThat(result).containsExactly(
      ElectricityMeterLogEntity.mock(
        channelId = remoteId,
        date = secondMeasurementDate,
        phase1Fae = 0.0020000003f,
        groupingString = "2023110100103",
        profileId = profileId
      ),
      ElectricityMeterLogEntity.mock(
        channelId = remoteId,
        date = thirdMeasurementDate,
        phase1Fae = 0f,
        counterReset = true,
        groupingString = "2023110100203",
        profileId = profileId
      ),
      ElectricityMeterLogEntity.mock(
        channelId = remoteId,
        date = fourthMeasurementDate,
        phase1Fae = 0.0014999999f,
        counterReset = false,
        groupingString = "2023110100303",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, electricityMeterLogRepository)
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    firstMeasurementDate: Date,
    totalCount: Int
  ) {
    val initialMeasurement = mockk<ElectricityMeasurement> {
      every { date } returns firstMeasurementDate
    }
    val response: Response<List<ElectricityMeasurement>> = mockk()

    every { response.code() } returns 200
    every { response.body() } returns listOf(initialMeasurement)
    every { response.headers() } returns Headers.headersOf("X-Total-Count", totalCount.toString())
    every { electricityMeterLogRepository.getInitialMeasurements(cloudService, remoteId) } returns response
  }
}
