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

import android.content.Context
import android.text.format.DateFormat
import io.mockk.*
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.storage.RuntimeStateHolder
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaTimerState
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.StartTimerUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
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
  private lateinit var runtimeStateHolder: RuntimeStateHolder

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK(relaxUnitFun = true)
  private lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

  @MockK
  private lateinit var context: Context

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: TimersDetailViewModel

  private val icon = ImageId(123)

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic(DateFormat::class)
    super.setUp()
    every { getChannelIconUseCase(any<ChannelDataEntity>()) } returns icon
    every { context.getString(R.string.hour_string_format) } returns "HH:mm:ss"
    every { DateFormat.format(any<String>(), any<Date>()) } returns "12:00:00"
  }

  @Test
  fun `should load channel without active timer`() {
    // given
    val remoteId = 123
    val channelData = channelData(remoteId = remoteId, online = true, on = false, extendedValue = null)
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)
    every { dateProvider.currentDate() } returns Date()
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      TimersDetailViewState(
        online = true,
        on = false,
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = icon,
          value = localizedString(R.string.details_timer_device_off)
        ),
        targetAction = TimerTargetAction.TURN_ON,
        icon = icon
      )
    )
  }

  @Test
  fun `should load channel with active timer`() {
    // given
    val remoteId = 123
    val startDate = Date()
    val currentTime: Date = mockk()
    every { dateProvider.currentDate() } returns currentTime

    val endDate: Date = mockk()
    every { endDate.after(currentTime) } returns true

    val channelData = channelData(
      remoteId = remoteId,
      online = true,
      on = false,
      extendedValue = createExtendedValueWithTimer(remoteId, endDate, startDate, true)
    )
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).hasSize(1)
    assertTimerState(
      states.first(),
      timerData = TimerProgressData(
        endTime = endDate,
        startTime = startDate,
        indeterminate = false,
        timerValue = TimerValue.OFF
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

    val channelData = channelData(
      remoteId = remoteId,
      online = true,
      on = false,
      extendedValue = createExtendedValueWithTimer(remoteId, endDate, null, false)
    )
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).hasSize(1)
    assertTimerState(
      states.first(),
      timerData = TimerProgressData(
        endTime = endDate,
        startTime = currentTime,
        indeterminate = true,
        timerValue = TimerValue.ON
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

    val channelData = channelData(
      remoteId = remoteId,
      online = true,
      on = false,
      extendedValue = createExtendedValueWithTimer(remoteId, endDate, null, false)
    )
    every { readChannelByRemoteIdUseCase(remoteId) } returns Maybe.just(channelData)
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.startEditMode()
    viewModel.loadData(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).hasSize(2)
    assertThat(states.first()).isEqualTo(TimersDetailViewState(editMode = true))
    assertTimerState(
      states.last(),
      timerData = TimerProgressData(
        endTime = endDate,
        startTime = currentTime,
        indeterminate = true,
        timerValue = TimerValue.ON
      )
    )
  }

  @Test
  fun `should start timer`() {
    // given
    val remoteId = 123
    val turnOn = true
    val duration = 345

    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0
    every { runtimeStateHolder.setLastTimerValue(remoteId, duration) } just Runs
    every { startTimerUseCase(remoteId, turnOn, duration) } returns Completable.complete()

    // when
    viewModel.onViewCreated(remoteId)
    viewModel.updateAction(TimerTargetAction.TURN_ON)
    viewModel.updateTimerTime(duration)
    states.clear()
    viewModel.onStartTimer()

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

    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0
    every { runtimeStateHolder.setLastTimerValue(remoteId, duration) } just Runs
    every { startTimerUseCase(remoteId, turnOn, duration) } returns Completable.error(StartTimerUseCase.InvalidTimeException())

    // when
    viewModel.onViewCreated(remoteId)
    viewModel.updateAction(TimerTargetAction.TURN_ON)
    viewModel.updateTimerTime(duration)
    states.clear()
    viewModel.onStartTimer()

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
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.onViewCreated(remoteId)
    states.clear()
    viewModel.stopTimer()

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
    every { runtimeStateHolder.getLastTimerValue(remoteId) } returns 0

    // when
    viewModel.onViewCreated(remoteId)
    states.clear()
    viewModel.cancelTimer()

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

  private fun channelData(
    remoteId: Int,
    online: Boolean,
    on: Boolean,
    extendedValue: ChannelExtendedValueEntity?
  ): ChannelDataEntity =
    ChannelDataEntity(
      channelEntity = ChannelEntity(
        id = 1,
        remoteId = remoteId,
        deviceId = 2,
        caption = "Channel",
        type = 0,
        function = SuplaFunction.POWER_SWITCH,
        visible = 1,
        locationId = 10,
        altIcon = 0,
        userIcon = 0,
        manufacturerId = 0,
        productId = 0,
        flags = 0,
        protocolVersion = 0,
        position = 0,
        profileId = 1
      ),
      channelValueEntity = channelValue(remoteId, online, on),
      locationEntity = LocationEntity(
        id = 1,
        remoteId = 10,
        caption = "Location",
        visible = 1,
        collapsed = 0,
        sorting = LocationSortingType.DEFAULT,
        sortOrder = 0,
        profileId = 1
      ),
      channelExtendedValueEntity = extendedValue,
      configEntity = null,
      stateEntity = null
    )

  private fun assertTimerState(state: TimersDetailViewState, timerData: TimerProgressData) {
    assertThat(state.online).isTrue()
    assertThat(state.on).isFalse()
    assertThat(state.timerData).isEqualTo(timerData)
    assertThat(state.targetAction).isEqualTo(TimerTargetAction.TURN_ON)
    assertThat(state.icon).isEqualTo(icon)
    assertThat(state.deviceStateData?.icon).isEqualTo(icon)
    assertThat(state.deviceStateData?.value).isEqualTo(localizedString(R.string.details_timer_device_off))

    val label = state.deviceStateData?.label as LocalizedString.WithResourceAndArguments
    assertThat(label.id).isEqualTo(R.string.details_timer_state_label_for_timer)
    assertThat(label.arguments).hasSize(1)
  }

  private fun channelValue(remoteId: Int, online: Boolean, on: Boolean): ChannelValueEntity {
    val status = SuplaChannelAvailabilityStatus.from(online)
    return mockk {
      every { channelRemoteId } returns remoteId
      every { this@mockk.status } returns status
      every { asRelayValue() } returns RelayValue(status, on, emptyList())
      every { isClosed() } returns on
    }
  }

  private fun createExtendedValueWithTimer(
    remoteId: Int,
    endTime: Date,
    startTime: Date?,
    expectedHiValue: Boolean
  ): ChannelExtendedValueEntity {
    val timerState: SuplaTimerState = mockk()
    every { timerState.countdownEndsAt } returns endTime
    every { timerState.expectedHiValue() } returns expectedHiValue

    val suplaExtendedValue: SuplaChannelExtendedValue = mockk()
    suplaExtendedValue.TimerStateValue = timerState

    val extendedValue: ChannelExtendedValueEntity = mockk()
    every { extendedValue.getSuplaValue() } returns suplaExtendedValue
    every { extendedValue.timerStartTime } returns startTime
    every { extendedValue.channelId } returns remoteId
    return extendedValue
  }
}
