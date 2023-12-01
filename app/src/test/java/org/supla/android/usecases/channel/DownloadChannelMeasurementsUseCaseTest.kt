package org.supla.android.usecases.channel
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

import androidx.work.ExistingWorkPolicy
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class DownloadChannelMeasurementsUseCaseTest {

  @Mock
  private lateinit var workManagerProxy: WorkManagerProxy

  @InjectMocks
  private lateinit var useCase: DownloadChannelMeasurementsUseCase

  @Test
  fun `should enqueue thermometer download`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val function = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

    // when
    useCase.invoke(remoteId, profileId, function)

    // then
    verify(workManagerProxy).enqueueUniqueWork(
      eq("DownloadTemperaturesWorker.$remoteId"),
      eq(ExistingWorkPolicy.KEEP),
      any()
    )
    verifyNoMoreInteractions(workManagerProxy)
  }

  @Test
  fun `should enqueue thermometer with humidity download`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val function = SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    // when
    useCase.invoke(remoteId, profileId, function)

    // then
    verify(workManagerProxy).enqueueUniqueWork(
      eq("DownloadTemperaturesAndHumidityWorker.$remoteId"),
      eq(ExistingWorkPolicy.KEEP),
      any()
    )
    verifyNoMoreInteractions(workManagerProxy)
  }

  @Test
  fun `should enqueue general purpose measurements download`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val function = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

    // when
    useCase.invoke(remoteId, profileId, function)

    // then
    verify(workManagerProxy).enqueueUniqueWork(
      eq("DownloadGeneralPurposeMeasurementsWorker.$remoteId"),
      eq(ExistingWorkPolicy.KEEP),
      any()
    )
    verifyNoMoreInteractions(workManagerProxy)
  }

  @Test
  fun `should enqueue general purpose counters download`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val function = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

    // when
    useCase.invoke(remoteId, profileId, function)

    // then
    verify(workManagerProxy).enqueueUniqueWork(
      eq("DownloadGeneralPurposeMeterWorker.$remoteId"),
      eq(ExistingWorkPolicy.KEEP),
      any()
    )
    verifyNoMoreInteractions(workManagerProxy)
  }
}
