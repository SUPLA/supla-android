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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelValue
import org.supla.android.db.Location
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.standarddetail.DetailPage
import org.supla.android.features.standarddetail.ItemBundle
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst.*
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.details.SwitchDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase

@RunWith(MockitoJUnitRunner::class)
class ChannelListViewModelTest : BaseViewModelTest<ChannelListViewState, ChannelListViewEvent, ChannelListViewModel>() {

  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var createProfileChannelsListUseCase: CreateProfileChannelsListUseCase

  @Mock
  private lateinit var channelActionUseCase: ChannelActionUseCase

  @Mock
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @Mock
  private lateinit var provideDetailTypeUseCase: ProvideDetailTypeUseCase

  @Mock
  private lateinit var findChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: ChannelListViewModel by lazy {
    ChannelListViewModel(
      channelRepository,
      createProfileChannelsListUseCase,
      channelActionUseCase,
      toggleLocationUseCase,
      provideDetailTypeUseCase,
      findChannelByRemoteIdUseCase,
      updateEventsManager,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(updateEventsManager.observeChannelsUpdate()).thenReturn(listsEventsSubject)
    super.setUp()
  }

  @Test
  fun `should load channels`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadChannels()

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase)
  }

  @Test
  fun `should toggle location collapsed and reload channels`() {
    // given
    val location = mockk<Location>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.CHANNEL)).thenReturn(Completable.complete())
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase, toggleLocationUseCase)
  }

  @Test
  fun `should swap when items are not null`() {
    // given
    val firstItemId = 123L
    val firstItemLocationId = 234
    val firstItem = mockk<ChannelBase>()
    every { firstItem.id } returns firstItemId
    every { firstItem.locationId } returns firstItemLocationId.toLong()

    val secondItemId = 345L
    val secondItem = mockk<ChannelBase>()
    every { secondItem.id } returns secondItemId

    whenever(channelRepository.reorderChannels(firstItemId, firstItemLocationId, secondItemId)).thenReturn(Completable.complete())

    // when
    viewModel.swapItems(firstItem, secondItem)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify(channelRepository).reorderChannels(firstItemId, firstItemLocationId, secondItemId)
    verifyNoMoreInteractions(channelRepository)
    verifyZeroInteractionsExcept(channelRepository)
  }

  @Test
  fun `should show valve dialog when action cannot be performed`() {
    // given
    val channelId = 123
    val buttonType = ButtonType.LEFT
    whenever(channelActionUseCase(channelId, buttonType)).thenReturn(Completable.error(ActionException.ChannelClosedManually(channelId)))

    // when
    viewModel.performAction(channelId, buttonType)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.ShowValveDialog(channelId)
    )
    verifyZeroInteractionsExcept(channelActionUseCase)
  }

  @Test
  fun `should show exceeded amperage dialog when action cannot be performed`() {
    // given
    val channelId = 123
    val buttonType = ButtonType.RIGHT
    whenever(channelActionUseCase(channelId, buttonType)).thenReturn(Completable.error(ActionException.ChannelExceedAmperage(channelId)))

    // when
    viewModel.performAction(channelId, buttonType)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.ShowAmperageExceededDialog(channelId)
    )
    verifyZeroInteractionsExcept(channelActionUseCase)
  }

  @Test
  fun `should not open RGB details when item is offline`() {
    // given
    val channel = mockk<Channel>()
    every { channel.onLine } returns false
    every { channel.func } returns SUPLA_CHANNELFNC_RGBLIGHTING

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open EM details when item is offline`() {
    // given
    val channelId = 123
    val channel = mockk<Channel>()
    every { channel.onLine } returns false
    every { channel.func } returns SUPLA_CHANNELFNC_ELECTRICITY_METER
    every { channel.channelId } returns channelId

    val detailType = LegacyDetailType.EM
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenLegacyDetails(channelId, detailType)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open details of switch with EM when item is offline`() {
    // given
    val channelId = 123
    val deviceId = 321
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH
    val channelValue = mockk<ChannelValue>()
    every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()

    val channel = mockk<Channel>()
    every { channel.onLine } returns false
    every { channel.func } returns function
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.value } returns channelValue
    every { channel.deviceID } returns deviceId

    val detailType = SwitchDetailType(listOf())
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSwitchDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), detailType.pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open thermostat details for channel with thermostat function`() {
    // given
    val channel = mockk<Channel>()
    every { channel.onLine } returns true
    every { channel.func } returns SUPLA_CHANNELFNC_THERMOSTAT

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetails
    )
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open legacy detail fragment`() {
    // given
    val channelId = 123
    val deviceId = 321
    val function = SUPLA_CHANNELFNC_THERMOMETER
    val channel = mockk<Channel>()
    every { channel.onLine } returns true
    every { channel.func } returns function
    every { channel.remoteId } returns channelId
    every { channel.deviceID } returns deviceId

    val detailType = ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermometerDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), detailType.pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open thermostat detail fragment when online`() {
    // given
    val channelId = 123
    val deviceId = 321
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val channel = mockk<Channel>()
    val pages = emptyList<DetailPage>()
    every { channel.onLine } returns true
    every { channel.func } returns function
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.deviceID } returns deviceId

    val thermostatDetailType = ThermostatDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(thermostatDetailType)

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open thermostat detail fragment when offline`() {
    // given
    val channelId = 123
    val deviceId = 321
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val channel = mockk<Channel>()
    val pages = emptyList<DetailPage>()
    every { channel.onLine } returns false
    every { channel.func } returns function
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.deviceID } returns deviceId

    val thermostatDetailType = ThermostatDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(thermostatDetailType)

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val channelId = 123
    val channelFunction = SUPLA_CHANNELFNC_NONE
    val channel = mockk<Channel>()
    every { channel.onLine } returns true
    every { channel.func } returns channelFunction
    every { channel.channelId } returns channelId

    // when
    viewModel.onListItemClick(channel)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase)
  }

  @Test
  fun `should load channel on update`() {
    // given
    val channelId = 223
    val channel: Channel = mockk()
    every { channel.remoteId } returns channelId
    whenever(findChannelByRemoteIdUseCase(channelId)).thenReturn(Maybe.just(channel))

    val suplaMessage: SuplaClientMsg = mockk()
    every { suplaMessage.channelId } returns channelId
    every { suplaMessage.type } returns SuplaClientMsg.onDataChanged

    val list = listOf(mockk<ListItem.ChannelItem>())
    every { list[0].channelBase } returns channel
    every { list[0].channelBase = channel } answers { }
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadChannels()
    viewModel.onSuplaMessage(suplaMessage)

    // then
    assertThat(states).containsExactly(ChannelListViewState(channels = list))
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase, findChannelByRemoteIdUseCase)
    io.mockk.verify { list[0].channelBase = channel }
  }

  @Test
  fun `should do nothing when update is not for channel`() {
    // given
    val suplaMessage: SuplaClientMsg = mockk()
    every { suplaMessage.channelId } returns 0
    every { suplaMessage.type } returns SuplaClientMsg.onDataChanged

    // when
    viewModel.onSuplaMessage(suplaMessage)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  private fun verifyZeroInteractionsExcept(vararg except: Any) {
    val allDependencies = listOf(
      channelRepository,
      createProfileChannelsListUseCase,
      channelActionUseCase,
      toggleLocationUseCase,
      provideDetailTypeUseCase
    )
    for (dependency in allDependencies) {
      if (!except.contains(dependency)) {
        verifyZeroInteractions(dependency)
      }
    }
  }
}
