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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst.*
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.GpmDetailType
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.details.RollerShutterDetailType
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
  private lateinit var gson: Gson

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
    val location = mockk<LocationEntity>()
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
    val firstItem = mockk<ChannelDataBase>()
    every { firstItem.id } returns firstItemId
    every { firstItem.locationId } returns firstItemLocationId

    val secondItemId = 345L
    val secondItem = mockk<ChannelDataBase>()
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
    val remoteId = 123
    val channel = mockChannelData(remoteId, SUPLA_CHANNELFNC_RGBLIGHTING)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open EM details when item is offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val channel = mockChannelData(remoteId, SUPLA_CHANNELFNC_ELECTRICITY_METER)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val detailType = LegacyDetailType.EM
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(remoteId)

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
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH
    val channel = mockChannelData(remoteId, function, deviceId, subValueType = SUBV_TYPE_IC_MEASUREMENTS.toShort())
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val detailType = SwitchDetailType(listOf())
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenSwitchDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), detailType.pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open legacy detail fragment`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_THERMOMETER
    val channel = mockChannelData(remoteId, function, deviceId)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val detailType = ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    whenever(provideDetailTypeUseCase(channel)).thenReturn(detailType)

    // when
    viewModel.onListItemClick(remoteId)

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
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, true)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val thermostatDetailType = ThermostatDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(thermostatDetailType)

    // when
    viewModel.onListItemClick(remoteId)

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
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val thermostatDetailType = ThermostatDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(thermostatDetailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenThermostatDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open GP measurement detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, true)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val gpmDetailType = GpmDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(gpmDetailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenGpmDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open GP measurement detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val gpmDetailType = GpmDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(gpmDetailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenGpmDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open GP meter detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, true)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val gpmDetailType = GpmDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(gpmDetailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenGpmDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open GP meter detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val gpmDetailType = GpmDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(gpmDetailType)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenGpmDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open roller shutter detail fragment when online`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, true)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val rollerShutterDetail = RollerShutterDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(rollerShutterDetail)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenRollerShutterDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open roller shutter detail fragment when offline`() {
    // given
    val remoteId = 123
    val channelId = 123
    val deviceId = 222
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
    val pages = emptyList<DetailPage>()
    val channel = mockChannelData(remoteId, function, deviceId, false)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    val rollerShutterDetail = RollerShutterDetailType(pages)
    whenever(provideDetailTypeUseCase(channel)).thenReturn(rollerShutterDetail)

    // when
    viewModel.onListItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenRollerShutterDetail(ItemBundle(channelId, deviceId, ItemType.CHANNEL, function), pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val remoteId = 123
    val channelFunction = SUPLA_CHANNELFNC_NONE
    val channel = mockChannelData(remoteId, channelFunction)
    whenever(findChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.onListItemClick(remoteId)

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

    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns channelId
    whenever(findChannelByRemoteIdUseCase.invoke(channelId)).thenReturn(Maybe.just(channel))

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

  private fun mockChannelData(
    remoteId: Int,
    function: Int,
    deviceId: Int? = null,
    online: Boolean = false,
    configEntity: ChannelConfigEntity? = null,
    subValueType: Short? = null
  ): ChannelDataEntity {
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
      every { isOnline() } returns online
    }

    return channel
  }
}
