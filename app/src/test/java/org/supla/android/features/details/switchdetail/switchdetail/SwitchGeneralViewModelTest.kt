package org.supla.android.features.details.switchdetail.switchdetail
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
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.ChannelConfigEventsManager.ConfigEvent
import org.supla.android.events.DownloadEventsManager
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterGeneralStateHandler
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterGeneralStateHandler
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewEvent
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewModel
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewState
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.testhelpers.extensions.mockShareable
import org.supla.android.tools.SuplaThreading
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channel.measurements.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import org.supla.android.usecases.channel.measurements.impulsecounter.LoadImpulseCounterMeasurementsUseCase
import org.supla.android.usecases.client.ExecuteRelayActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import java.util.Date
import java.util.concurrent.TimeUnit

class SwitchGeneralViewModelTest :
  BaseViewModelTest<SwitchGeneralViewState, SwitchGeneralViewEvent, SwitchGeneralViewModel>(MockSchedulers.MOCKK) {

  @MockK
  private lateinit var loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase

  @MockK
  private lateinit var loadImpulseCounterMeasurementsUseCase: LoadImpulseCounterMeasurementsUseCase

  @MockK
  private lateinit var electricityMeterGeneralStateHandler: ElectricityMeterGeneralStateHandler

  @MockK
  private lateinit var downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase

  @MockK
  private lateinit var impulseCounterGeneralStateHandler: ImpulseCounterGeneralStateHandler

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK
  private lateinit var executeRelayActionUseCase: ExecuteRelayActionUseCase

  @MockK
  private lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase

  @MockK
  private lateinit var downloadEventsManager: DownloadEventsManager

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var preferences: ApplicationPreferences

  @MockK
  private lateinit var getChannelStateUseCase: GetChannelStateUseCase

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @RelaxedMockK
  private lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

  @MockK
  override lateinit var threading: SuplaThreading

  @InjectMockKs
  override lateinit var viewModel: SwitchGeneralViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { suplaClientProvider.provide() } returns null
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.POWER_SWITCH
    val channelData = mockChannelData(remoteId, function)
    val stateIcon: ImageId = mockk()
    val onIcon: ImageId = mockk()
    val offIcon: ImageId = mockk()
    val channelState: ChannelState = mockk {
      every { isActive } returns true
      every { value } returns ChannelState.Value.ON
    }

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { getChannelStateUseCase.invoke(channelData) } returns channelState
    every { getChannelIconUseCase.invoke(channelData) } returns stateIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON) } returns onIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF) } returns offIcon
    every { dateProvider.currentDate() } returns Date()
    every { electricityMeterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { impulseCounterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { getAllChannelIssuesUseCase.invoke(any()) } returns emptyList()
    every { preferences.scale } returns 1f

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting(
        { it.leftButtonDisabled },
        { it.deviceStateData?.label },
        { it.deviceStateData?.icon },
        { it.deviceStateData?.value },
        { it.rightButtonState?.icon },
        { it.leftButtonState?.icon },
        { it.electricityMeterState },
      )
      .containsExactly(
        tuple(
          false,
          localizedString(R.string.details_timer_state_label),
          stateIcon,
          localizedString(R.string.details_timer_device_on),
          onIcon,
          offIcon,
          null
        )
      )

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      getChannelStateUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF)
      dateProvider.currentDate()
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      getChannelStateUseCase,
      getChannelIconUseCase,
      dateProvider,
      loadElectricityMeterMeasurementsUseCase,
      readGroupWithChannelsUseCase
    )
  }

  @Test
  fun `should load channel - without buttons`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.PUMP_SWITCH
    val channelData = mockChannelData(remoteId, function)
    val stateIcon: ImageId = mockk()

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { getChannelStateUseCase.invoke(channelData) } returns mockk {
      every { isActive } returns true
      every { value } returns ChannelState.Value.ON
    }
    every { getChannelIconUseCase.invoke(channelData) } returns stateIcon
    every { dateProvider.currentDate() } returns Date()
    every { electricityMeterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { impulseCounterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { getAllChannelIssuesUseCase.invoke(any()) } returns emptyList()
    every { preferences.scale } returns 1f

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting(
        { it.leftButtonDisabled },
        { it.deviceStateData?.label },
        { it.deviceStateData?.icon },
        { it.deviceStateData?.value },
        { it.rightButtonState?.icon },
        { it.leftButtonState?.icon },
        { it.electricityMeterState },
      )
      .containsExactly(
        tuple(
          false,
          localizedString(R.string.details_timer_state_label),
          stateIcon,
          localizedString(R.string.details_timer_device_on),
          null,
          null,
          null
        )
      )

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      getChannelStateUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData)
      dateProvider.currentDate()
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      getChannelStateUseCase,
      getChannelIconUseCase,
      dateProvider,
      loadElectricityMeterMeasurementsUseCase,
      readGroupWithChannelsUseCase
    )
  }

  @Test
  fun `should show relay program info for online channel with weekly schedule enabled`() {
    // given
    val remoteId = 123
    val channelData = mockChannelData(remoteId, SuplaFunction.POWER_SWITCH, weeklyScheduleEnabled = true)
    val configEvents = PublishSubject.create<ConfigEvent>()
    every { threading.schedulers.computation } returns testScheduler
    every { channelConfigEventsManager.observerConfig(remoteId) } returns configEvents
    every { downloadEventsManager.observeProgress(remoteId) } returns Observable.never()
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { getChannelStateUseCase.invoke(channelData) } returns mockk {
      every { isActive } returns true
      every { value } returns ChannelState.Value.ON
    }
    every { getChannelIconUseCase.invoke(channelData) } returns mockk()
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = any()) } returns mockk()
    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 10
    every { dateProvider.currentMinute() } returns 2 andThen 17
    every { dateProvider.currentDate() } returns Date()
    every { electricityMeterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { impulseCounterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { getAllChannelIssuesUseCase.invoke(any()) } returns emptyList()
    every { preferences.scale } returns 1f

    // when
    viewModel.onViewCreated(remoteId, ItemType.CHANNEL)
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    configEvents.onNext(
      ConfigEvent(
        ConfigResult.RESULT_TRUE,
        SuplaChannelWeeklyScheduleConfig(
          remoteId,
          null,
          1L,
          emptyList(),
          listOf(SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 10, QuarterOfHour.FIRST, SuplaScheduleProgram.OFF))
        )
      )
    )

    // then
    assertThat(states.last().programInfo).hasSize(1)
    assertThat(states.last().programInfo.single().description).isEqualTo(localizedString(R.string.turn_off))
    verify(exactly = 1) { readChannelWithChildrenUseCase.invoke(remoteId) }

    // when
    testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

    // then
    verify(exactly = 2) { dateProvider.currentMinute() }

    // when
    every { channelData.status } returns SuplaChannelAvailabilityStatus.OFFLINE
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

    // then
    assertThat(states.last().programInfo).isEmpty()

    // when
    every { channelData.status } returns SuplaChannelAvailabilityStatus.ONLINE
    every { channelData.channel.channelValueEntity.asRelayValue() } returns
      RelayValue(SuplaChannelAvailabilityStatus.OFFLINE, false, emptyList(), SuplaRelayMode.NOT_SET)
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

    // then
    assertThat(states.last().programInfo).isEmpty()
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.POWER_SWITCH
    val group: ChannelGroupDataEntity = mockk {
      every { this@mockk.function } returns function
      every { this@mockk.remoteId } returns remoteId
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { channelGroupEntity } returns mockk {
        every { groupTotalValues } returns emptyList()
      }
    }
    val onIcon: ImageId = mockk()
    val offIcon: ImageId = mockk()

    every { readGroupWithChannelsUseCase(remoteId) } returns Observable.just(GroupWithChannels(group, emptyList()))
    every { getChannelStateUseCase.invoke(group) } returns mockk { every { isActive } returns true }
    every { getChannelIconUseCase.invoke(group, channelStateValue = ChannelState.Value.ON) } returns onIcon
    every { getChannelIconUseCase.invoke(group, channelStateValue = ChannelState.Value.OFF) } returns offIcon
    every { preferences.scale } returns 1f

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting(
        { it.leftButtonDisabled },
        { it.deviceStateData?.label },
        { it.deviceStateData?.icon },
        { it.deviceStateData?.value },
        { it.rightButtonState?.icon },
        { it.leftButtonState?.icon },
        { it.electricityMeterState }
      )
      .containsExactly(
        tuple(
          false,
          null,
          null,
          null,
          onIcon,
          offIcon,
          null
        )
      )

    verify {
      readGroupWithChannelsUseCase(remoteId)
      getChannelIconUseCase.invoke(group, channelStateValue = ChannelState.Value.ON)
      getChannelIconUseCase.invoke(group, channelStateValue = ChannelState.Value.OFF)
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      getChannelStateUseCase,
      getChannelIconUseCase,
      dateProvider,
      loadElectricityMeterMeasurementsUseCase,
      readGroupWithChannelsUseCase
    )
  }

  @Test
  fun `should load estimated count down end time`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.LIGHTSWITCH
    val stateIcon: ImageId = mockk()
    val onIcon: ImageId = mockk()
    val offIcon: ImageId = mockk()

    val estimatedEndDate = Date(1000)
    every { dateProvider.currentDate() } returns Date(100)
    val channelState: ChannelState = mockk {
      every { isActive } returns true
      every { value } returns ChannelState.Value.ON
    }

    val channelData = mockChannelData(remoteId, function, estimatedEndDate)

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { getChannelStateUseCase.invoke(channelData) } returns channelState
    every { getChannelIconUseCase.invoke(channelData) } returns stateIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON) } returns onIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF) } returns offIcon
    every { electricityMeterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { impulseCounterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { getAllChannelIssuesUseCase.invoke(any()) } returns emptyList()
    every { preferences.scale } returns 1f

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting(
        { it.leftButtonDisabled },
        { it.deviceStateData?.label },
        { it.deviceStateData?.icon },
        { it.deviceStateData?.value },
        { it.rightButtonState?.icon },
        { it.leftButtonState?.icon },
        { it.electricityMeterState }
      )
      .containsExactly(
        tuple(
          false,
          LocalizedString.WithResourceAndDate(R.string.details_timer_state_label_for_timer, 1000),
          stateIcon,
          localizedString(R.string.details_timer_device_on),
          onIcon,
          offIcon,
          null
        )
      )

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      getChannelStateUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF)
      dateProvider.currentDate()
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      getChannelStateUseCase,
      getChannelIconUseCase,
      dateProvider,
      loadElectricityMeterMeasurementsUseCase,
      readGroupWithChannelsUseCase
    )
  }

  @Test
  fun `shouldn't load estimated countdown end time when time elapsed`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.LIGHTSWITCH
    val stateIcon: ImageId = mockk()
    val onIcon: ImageId = mockk()
    val offIcon: ImageId = mockk()

    val estimatedEndDate = Date(1000)
    every { dateProvider.currentDate() } returns Date(1003)

    val channelState: ChannelState = mockk {
      every { isActive } returns true
      every { value } returns ChannelState.Value.ON
    }

    val channelData = mockChannelData(remoteId, function, estimatedEndDate)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { getChannelStateUseCase.invoke(channelData) } returns channelState
    every { getChannelIconUseCase.invoke(channelData) } returns stateIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON) } returns onIcon
    every { getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF) } returns offIcon
    every { electricityMeterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { impulseCounterGeneralStateHandler.updateState(any(), any(), any()) } answers { firstArg() }
    every { getAllChannelIssuesUseCase.invoke(any()) } returns emptyList()
    every { preferences.scale } returns 1f

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting(
        { it.leftButtonDisabled },
        { it.deviceStateData?.label },
        { it.deviceStateData?.icon },
        { it.deviceStateData?.value },
        { it.rightButtonState?.icon },
        { it.leftButtonState?.icon },
        { it.electricityMeterState }
      )
      .containsExactly(
        tuple(
          false,
          localizedString(R.string.details_timer_state_label),
          stateIcon,
          localizedString(R.string.details_timer_device_on),
          onIcon,
          offIcon,
          null
        )
      )

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      getChannelStateUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.ON)
      getChannelIconUseCase.invoke(channelData, channelStateValue = ChannelState.Value.OFF)
      dateProvider.currentDate()
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      getChannelStateUseCase,
      getChannelIconUseCase,
      dateProvider,
      loadElectricityMeterMeasurementsUseCase,
      readGroupWithChannelsUseCase
    )
  }

  private fun mockTimerState(date: Date): ChannelExtendedValueEntity {
    val suplaExtendedValue: SuplaChannelExtendedValue = mockk()
    every { suplaExtendedValue.timerEstimatedEndDate } returns date

    val extendedValue: ChannelExtendedValueEntity = mockk()
    every { extendedValue.getSuplaValue() } returns suplaExtendedValue

    return extendedValue
  }

  private fun mockChannelData(
    remoteId: Int,
    function: SuplaFunction,
    estimatedEndDate: Date? = null,
    weeklyScheduleEnabled: Boolean = false
  ): ChannelWithChildren {
    val channel: ChannelDataEntity = mockk {
      mockShareable(remoteId = remoteId, function = function)
      every { flags } returns 0
      every { channelValueEntity } returns mockk {
        every { asRelayValue() } returns RelayValue(
          SuplaChannelAvailabilityStatus.OFFLINE,
          false,
          if (weeklyScheduleEnabled) listOf(SuplaRelayFlag.WEEKLY_SCHEDULE_ENABLED) else emptyList(),
          SuplaRelayMode.NOT_SET
        )
        every { getValueAsByteArray() } returns byteArrayOf()
      }
      every { channelExtendedValueEntity } returns estimatedEndDate?.let { mockTimerState(estimatedEndDate) }
    }

    return mockk {
      every { this@mockk.channel } returns channel
      every { children } returns emptyList()
      every { isOrHasElectricityMeter } returns false
      every { isOrHasImpulseCounter } returns false
      every { this@mockk.function } returns function
      every { this@mockk.remoteId } returns remoteId
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
  }
}
