package org.supla.android.features.details.switchdetail.timer
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

import io.mockk.*
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.db.Channel
import org.supla.android.db.ChannelExtendedValue
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaTimerState
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.StartTimerUseCase
import java.util.*

class TimersDetailViewModelTest : BaseViewModelTest<TimersDetailViewState, TimersDetailViewEvent, TimersDetailViewModel>() {

  @MockK
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK
  private lateinit var startTimerUseCase: StartTimerUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: TimersDetailViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load channel without active timer`() {
    // given
    val remoteId = 123
    val channel: Channel = mockk()
    every { channel.extendedValue } returns null
    val channelData: ChannelDataEntity = mockk { every { getLegacyChannel() } returns channel }
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)
    every { dateProvider.currentDate() } returns Date()

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(null, channel, false, TimerTargetAction.TURN_ON)
    )
  }

  @Test
  fun `should load channel with active timer`() {
    // given
    val remoteId = 123

    val startDate = Date()
    val startTimestamp = startDate.time

    val currentTime: Date = mockk()
    every { dateProvider.currentDate() } returns currentTime

    val endDate: Date = mockk()
    every { endDate.after(currentTime) } returns true

    val channel: Channel = mockk()
    every { channel.extendedValue } returns createExtendedValueWithTimer(endDate, startTimestamp, true)
    val channelData: ChannelDataEntity = mockk { every { getLegacyChannel() } returns channel }
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(
        TimerProgressData(
          endTime = endDate,
          startTime = startDate,
          indeterminate = false,
          timerValue = TimerValue.OFF
        ),
        channel,
        false,
        TimerTargetAction.TURN_ON
      )
    )
  }

  @Test
  fun `should load channel with active timer and indeterminate progress`() {
    // given
    val remoteId = 123

    val currentTime: Date = mockk()
    every { dateProvider.currentDate() } returns currentTime

    val endDate: Date = mockk()
    every { endDate.after(currentTime) } returns true

    val channel: Channel = mockk()
    every { channel.extendedValue } returns createExtendedValueWithTimer(endDate, null, false)
    val channelData: ChannelDataEntity = mockk { every { getLegacyChannel() } returns channel }
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(
        TimerProgressData(
          endTime = endDate,
          startTime = currentTime,
          indeterminate = true,
          timerValue = TimerValue.ON
        ),
        channel,
        false,
        TimerTargetAction.TURN_ON
      )
    )
  }

  @Test
  fun `should cleanup edit mode when loading channel data`() {
    // given
    val remoteId = 123

    val currentTime: Date = mockk()
    every { dateProvider.currentDate() } returns currentTime

    val endDate: Date = mockk()
    every { endDate.after(currentTime) } returns true

    val channel: Channel = mockk()
    every { channel.extendedValue } returns createExtendedValueWithTimer(endDate, null, false)
    val channelData: ChannelDataEntity = mockk { every { getLegacyChannel() } returns channel }
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.startEditMode()
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(editMode = true),
      TimersDetailViewState(
        TimerProgressData(
          endTime = endDate,
          startTime = currentTime,
          indeterminate = true,
          timerValue = TimerValue.ON
        ),
        channel,
        false,
        TimerTargetAction.TURN_ON
      )
    )
  }

  @Test
  fun `should start timer`() {
    // given
    val remoteId = 123
    val turnOn = true
    val duration = 345

    every { startTimerUseCase(remoteId, turnOn, duration) } returns Completable.complete()

    // when
    viewModel.startTimer(remoteId, turnOn, duration)

    // then
    assertThat(events).isEmpty()
    assertThat(states).isEmpty()
    verify { startTimerUseCase.invoke(remoteId, turnOn, duration) }
    confirmVerified(startTimerUseCase)
    verify {
      readChannelByRemoteIdUseCase wasNot Called
      dateProvider wasNot Called
      executeSimpleActionUseCase wasNot Called
    }
  }

  @Test
  fun `should inform about invalid time`() {
    // given
    val remoteId = 123
    val turnOn = true
    val duration = 345

    every { startTimerUseCase(remoteId, turnOn, duration) } returns Completable.error(StartTimerUseCase.InvalidTimeException())

    // when
    viewModel.startTimer(remoteId, turnOn, duration)

    // then
    assertThat(events).containsExactly(TimersDetailViewEvent.ShowInvalidTimeToast)
    assertThat(states).isEmpty()
    verify { startTimerUseCase.invoke(remoteId, turnOn, duration) }
    confirmVerified(startTimerUseCase)
    verify {
      readChannelByRemoteIdUseCase wasNot Called
      dateProvider wasNot Called
      executeSimpleActionUseCase wasNot Called
    }
  }

  @Test
  fun `should stop timer`() {
    // given
    val remoteId = 123

    val channelValue: ChannelValueEntity = mockk {
      every { isClosed() } returns true
    }
    val channel: ChannelDataEntity = mockk {
      every { channelValueEntity } returns channelValue
    }
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channel)

    every { executeSimpleActionUseCase(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId) } returns Completable.complete()

    // when
    viewModel.stopTimer(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).isEmpty()

    verify { readChannelByRemoteIdUseCase.invoke(remoteId) }
    verify { executeSimpleActionUseCase.invoke(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId) }
    confirmVerified(readChannelByRemoteIdUseCase, executeSimpleActionUseCase)
    verify {
      dateProvider wasNot Called
      startTimerUseCase wasNot Called
    }
  }

  @Test
  fun `should cancel timer`() {
    // given
    val remoteId = 123

    val channelValue: ChannelValueEntity = mockk {
      every { isClosed() } returns false
    }
    val channel: ChannelDataEntity = mockk {
      every { channelValueEntity } returns channelValue
    }
    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channel)

    every { executeSimpleActionUseCase(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId) } returns Completable.complete()

    // when
    viewModel.cancelTimer(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).isEmpty()

    verify { readChannelByRemoteIdUseCase.invoke(remoteId) }
    verify { executeSimpleActionUseCase.invoke(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId) }
    confirmVerified(readChannelByRemoteIdUseCase, executeSimpleActionUseCase)
    verify {
      dateProvider wasNot Called
      startTimerUseCase wasNot Called
    }
  }

  @Test
  fun `should cancel edit mode`() {
    // when
    viewModel.startEditMode()
    viewModel.cancelEditMode()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(editMode = true),
      TimersDetailViewState()
    )
  }

  @Test
  fun `should calculate progress view data`() {
    // given
    val startDate = Date(120 * 1000)
    val endDate = Date((120 + 7200 + 240 + 14) * 1000)

    every { dateProvider.currentTimestamp() } returns (120L + 3600 + 120 + 7) * 1000

    // when
    val data = viewModel.calculateProgressViewData(startDate, endDate)

    // then
    assertThat(data.progress).isEqualTo(0.5f)
    assertThat(data.leftTimeValues)
      .extracting("hours", "minutes", "seconds")
      .containsExactly(1, 2, 8)
  }

  private fun createExtendedValueWithTimer(endTime: Date, startTimestamp: Long?, expectedHiValue: Boolean): ChannelExtendedValue {
    val timerState: SuplaTimerState = mockk()
    every { timerState.countdownEndsAt } returns endTime
    every { timerState.expectedHiValue() } returns expectedHiValue

    val suplaExtendedValue: SuplaChannelExtendedValue = mockk()
    suplaExtendedValue.TimerStateValue = timerState

    val extendedValue: ChannelExtendedValue = mockk()
    every { extendedValue.extendedValue } returns suplaExtendedValue
    every { extendedValue.timerStartTimestamp } returns startTimestamp
    return extendedValue
  }
}
