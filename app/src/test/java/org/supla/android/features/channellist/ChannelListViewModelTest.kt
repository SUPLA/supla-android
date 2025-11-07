package org.supla.android.features.channellist
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

import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.ActionAlertDialogState
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.CreateProfileChannelsListUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.GpmDetailType
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.SwitchDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.details.WindowDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

class ChannelListViewModelTest : BaseViewModelTest<ChannelListViewState, ChannelListViewEvent, ChannelListViewModel>(MockSchedulers.MOCKK) {

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var createProfileChannelsListUseCase: CreateProfileChannelsListUseCase

  @MockK
  private lateinit var channelActionUseCase: ChannelActionUseCase

  @MockK
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @MockK
  private lateinit var provideDetailTypeUseCase: ProvideChannelDetailTypeUseCase

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var gson: Gson

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK(relaxed = true)
  private lateinit var preferences: Preferences

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: ChannelListViewModel by lazy {
    ChannelListViewModel(
      createProfileChannelsListUseCase,
      provideDetailTypeUseCase,
      readChannelWithChildrenUseCase,
      executeSimpleActionUseCase,
      toggleLocationUseCase,
      channelActionUseCase,
      channelRepository,
      updateEventsManager,
      dateProvider,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { updateEventsManager.observeChannelsUpdate() } returns listsEventsSubject
    super.setUp()
  }

  @Test
  fun `should load channels`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    every { createProfileChannelsListUseCase() } returns Observable.just(list)

    // when
    viewModel.loadChannels()

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()

    verify { createProfileChannelsListUseCase() }
    confirmDependenciesVerified()
  }

  @Test
  fun `should toggle location collapsed and reload channels`() {
    // given
    val location = mockk<LocationEntity>()
    every { toggleLocationUseCase(location, CollapsedFlag.CHANNEL) } returns Completable.complete()
    val list = listOf(mockk<ListItem.ChannelItem>())
    every { createProfileChannelsListUseCase() } returns Observable.just(list)

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()

    verify {
      toggleLocationUseCase(location, CollapsedFlag.CHANNEL)
      createProfileChannelsListUseCase()
    }
    confirmDependenciesVerified()
  }

  @Test
  fun `should swap when items are not null`() {
    // given
    val firstItemId = 123L
    val firstItemLocationId = 234
    val firstItem = mockk<ChannelDataBase>()
    every { firstItem.id } returns firstItemId
    every { firstItem.locationId } returns firstItemLocationId

    val secondItemId = 345L
    val secondItem = mockk<ChannelDataBase>()
    every { secondItem.id } returns secondItemId

    every { channelRepository.reorderChannels(firstItemId, firstItemLocationId, secondItemId) } returns Completable.complete()

    // when
    viewModel.swapItems(firstItem, secondItem)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify {
      channelRepository.reorderChannels(firstItemId, firstItemLocationId, secondItemId)
    }
    confirmDependenciesVerified()
  }

  @Test
  fun `should show valve dialog when action cannot be performed`() {
    // given
    val channelId = 123
    val buttonType = ButtonType.LEFT
    every { channelActionUseCase(channelId, buttonType) } returns Completable.error(ActionException.ValveClosedManually(channelId))

    // when
    viewModel.performAction(channelId, buttonType)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.valve_warning_manually_closed,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = ActionId.OPEN,
          remoteId = channelId
        )
      )
    )
    assertThat(events).isEmpty()

    verify { channelActionUseCase(channelId, buttonType) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should show exceeded amperage dialog when action cannot be performed`() {
    // given
    val channelId = 123
    val buttonType = ButtonType.RIGHT
    every { channelActionUseCase(channelId, buttonType) } returns Completable.error(ActionException.ChannelExceedAmperage(channelId))

    // when
    viewModel.performAction(channelId, buttonType)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.overcurrent_question,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = ActionId.TURN_ON,
          remoteId = channelId
        )
      )
    )

    verify { channelActionUseCase(channelId, buttonType) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should not open RGB details when item is offline`() {
    // given
    val remoteId = 123
    val channel = mockChannelData(remoteId, SuplaFunction.RGB_LIGHTING)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    confirmDependenciesVerified()
  }

  @Test
  fun `should open details of switch with EM when item is offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.LIGHTSWITCH
    val channel = mockChannelData(remoteId, function, deviceId, subValueType = SUBV_TYPE_IC_MEASUREMENTS.toShort())
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val detailType = SwitchDetailType(listOf())
    every { provideDetailTypeUseCase(channel) } returns detailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenStandardDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), detailType.pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open thermometer detail fragment`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.THERMOMETER
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val detailType = ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    every { provideDetailTypeUseCase(channel) } returns detailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), detailType.pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open thermostat detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.HVAC_THERMOSTAT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, SuplaChannelAvailabilityStatus.ONLINE)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val thermostatDetailType = ThermostatDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns thermostatDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open thermostat detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.HVAC_THERMOSTAT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val thermostatDetailType = ThermostatDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns thermostatDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open GP measurement detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.GENERAL_PURPOSE_MEASUREMENT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, SuplaChannelAvailabilityStatus.ONLINE)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val gpmDetailType = GpmDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns gpmDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open GP measurement detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.GENERAL_PURPOSE_MEASUREMENT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val gpmDetailType = GpmDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns gpmDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open GP meter detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.GENERAL_PURPOSE_METER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, SuplaChannelAvailabilityStatus.ONLINE)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val gpmDetailType = GpmDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns gpmDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )

    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open GP meter detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.GENERAL_PURPOSE_METER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val gpmDetailType = GpmDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns gpmDetailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open roller shutter detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, SuplaChannelAvailabilityStatus.ONLINE)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val rollerShutterDetail = WindowDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns rollerShutterDetail
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenStandardDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should open roller shutter detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)

    val rollerShutterDetail = WindowDetailType(pages)
    every { provideDetailTypeUseCase(channel) } returns rollerShutterDetail
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenStandardDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verify { provideDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val remoteId = 123
    val channelFunction = SuplaFunction.NONE
    val channel = mockChannelData(remoteId, channelFunction)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    confirmDependenciesVerified()
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    every { createProfileChannelsListUseCase() } returns Observable.just(list)

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()
    verify { createProfileChannelsListUseCase() }
    confirmDependenciesVerified()
  }

  @Test
  fun `should not allow to process event to fast`() {
    // given
    every { dateProvider.currentTimestamp() } returns 10

    // when
    viewModel.onListItemClick(1)

    // then
    confirmVerified(readChannelWithChildrenUseCase)
  }

  private fun confirmDependenciesVerified() {
    val allDependencies = listOf(
      channelRepository,
      createProfileChannelsListUseCase,
      channelActionUseCase,
      toggleLocationUseCase,
      provideDetailTypeUseCase
    )
    for (dependency in allDependencies) {
      confirmVerified(dependency)
    }
  }

  private fun mockChannelData(
    remoteId: Int,
    function: SuplaFunction,
    deviceId: Int? = null,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    configEntity: ChannelConfigEntity? = null,
    subValueType: Short? = null
  ): ChannelWithChildren {
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.deviceId } returns deviceId
    }
    val channelValueEntity: ChannelValueEntity = mockk {
      every { this@mockk.subValueType } returns (subValueType ?: 0)
    }
    val channel = mockk<ChannelDataEntity> {
      every { this@mockk.function } returns function
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.channelEntity } returns channelEntity
      every { this@mockk.channelValueEntity } returns channelValueEntity
      every { this@mockk.configEntity } returns configEntity
      every { this@mockk.status } returns status
    }

    return ChannelWithChildren(channel, emptyList())
  }
}
