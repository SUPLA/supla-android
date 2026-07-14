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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Rule
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.ActionAlertDialogState
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.ChannelToListItemMapper
import org.supla.android.usecases.channel.CreateProfileChannelsListUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channel.ReorderChannelsUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

class ChannelListViewModelTest : BaseViewModelTest<ChannelListViewState, ChannelListViewEvent, ChannelListViewModel>(MockSchedulers.MOCKK) {
  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var createProfileChannelsListUseCase: CreateProfileChannelsListUseCase

  @MockK
  private lateinit var channelActionUseCase: ChannelActionUseCase

  @MockK
  private lateinit var reorderChannelsUseCase: ReorderChannelsUseCase

  @MockK
  private lateinit var channelToListItemMapper: ChannelToListItemMapper

  @MockK
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @MockK
  private lateinit var provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: ChannelListViewModel by lazy {
    ChannelListViewModel(
      createProfileChannelsListUseCase,
      provideChannelDetailTypeUseCase,
      readChannelWithChildrenUseCase,
      executeSimpleActionUseCase,
      channelToListItemMapper,
      reorderChannelsUseCase,
      toggleLocationUseCase,
      channelActionUseCase,
      updateEventsManager,
      vibrationHelper,
      dateProvider,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()
  private val profileId: Long = 1

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { updateEventsManager.observeChannelsUpdate() } returns listsEventsSubject
    every { updateEventsManager.observeAllChannels() } returns Observable.empty()
    super.setUp()
  }

  @Test
  fun `should load channels`() {
    // given
    val list = listOf(mockk<ListItem.DefaultItem>())
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
    val locationId = 123
    every { toggleLocationUseCase(locationId, CollapsedFlag.CHANNEL) } returns Completable.complete()
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileChannelsListUseCase() } returns Observable.just(list)

    // when
    viewModel.onLocationClick(locationId)

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(channels = list)
    )
    assertThat(events).isEmpty()

    verify {
      toggleLocationUseCase(locationId, CollapsedFlag.CHANNEL)
      createProfileChannelsListUseCase()
    }
    confirmDependenciesVerified()
  }

  @Test
  fun `should swap when items are in same location`() {
    // given
    val firstItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val items = listOf(firstItem, secondItem, thirdItem)
    viewModel.setState(ChannelListViewState(channels = items))

    // when
    viewModel.moveItems(0, 2)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(channels = listOf(firstItem, secondItem, thirdItem)),
      ChannelListViewState(channels = listOf(secondItem, thirdItem, firstItem))
    )
    assertThat(events).isEmpty()

    confirmDependenciesVerified()
  }

  @Test
  fun `should not swap when items are in different location`() {
    // given
    val firstItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "2"
    }
    val items = listOf(firstItem, secondItem, thirdItem)
    viewModel.setState(ChannelListViewState(channels = items))

    // when
    viewModel.moveItems(0, 2)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(channels = listOf(firstItem, secondItem, thirdItem)),
    )
    assertThat(events).isEmpty()

    confirmDependenciesVerified()
  }

  @Test
  fun `should not swap when different items`() {
    // given
    val firstItem: ListItem.LocationItem = mockk()
    val secondItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val items = listOf(firstItem, secondItem, thirdItem)
    viewModel.setState(ChannelListViewState(channels = items))

    // when
    viewModel.moveItems(2, 0)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(channels = listOf(firstItem, secondItem, thirdItem)),
    )
    assertThat(events).isEmpty()

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
  fun `should open RGB details when item is offline`() {
    // given
    val remoteId = 123
    val deviceId = 234
    val function = SuplaFunction.RGB_LIGHTING
    val channel = mockChannelData(remoteId, function, deviceId)
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { dateProvider.currentTimestamp() } returns 500

    val detailType = StandardDetailType(listOf())
    every { provideChannelDetailTypeUseCase(channel) } returns detailType

    // when
    viewModel.onItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenDetail(ItemBundle(remoteId, deviceId, profileId, ItemType.CHANNEL, function), detailType.pages)
    )

    verify { provideChannelDetailTypeUseCase(channel) }
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

    val detailType = StandardDetailType(listOf())
    every { provideChannelDetailTypeUseCase(channel) } returns detailType
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenDetail(ItemBundle(channelId, deviceId, profileId, ItemType.CHANNEL, function), detailType.pages)
    )

    verify { provideChannelDetailTypeUseCase(channel) }
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

    val rollerShutterDetail = StandardDetailType(pages)
    every { provideChannelDetailTypeUseCase(channel) } returns rollerShutterDetail
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenDetail(ItemBundle(channelId, deviceId, profileId, ItemType.CHANNEL, function), pages)
    )
    verify { provideChannelDetailTypeUseCase(channel) }
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

    val rollerShutterDetail = StandardDetailType(pages)
    every { provideChannelDetailTypeUseCase(channel) } returns rollerShutterDetail
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ChannelListViewEvent.OpenDetail(ItemBundle(channelId, deviceId, profileId, ItemType.CHANNEL, function), pages)
    )
    verify { provideChannelDetailTypeUseCase(channel) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val remoteId = 123
    val channelFunction = SuplaFunction.NONE
    val channel = mockChannelData(remoteId, channelFunction, status = SuplaChannelAvailabilityStatus.ONLINE)
    every { provideChannelDetailTypeUseCase(channel) } returns null
    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onItemClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify {
      provideChannelDetailTypeUseCase.invoke(channel)
    }
    confirmDependenciesVerified()
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.DefaultItem>())
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
  fun `should set filter text and reload channels`() {
    // given
    val filterText = "kitchen"
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileChannelsListUseCase(filterString = filterText) } returns Observable.just(list)

    // when
    viewModel.handle(TopBarSearchEvent.QueryChange(filterText))

    // then
    assertThat(viewModel.searchData.query).isEqualTo(filterText)
    assertThat(states).containsExactly(
      ChannelListViewState(channels = list)
    )
    assertThat(events).isEmpty()

    verify { createProfileChannelsListUseCase(filterString = filterText) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should dismiss action dialog on force action and execute requested action`() {
    // given
    val remoteId = 123
    val actionId = ActionId.TURN_ON
    every { executeSimpleActionUseCase(actionId, SubjectType.CHANNEL, remoteId) } returns Completable.complete()
    viewModel.setState(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.overcurrent_question,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = actionId,
          remoteId = remoteId
        )
      )
    )
    states.clear()

    // when
    viewModel.forceAction(remoteId, actionId)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    verify { executeSimpleActionUseCase(actionId, SubjectType.CHANNEL, remoteId) }
    confirmDependenciesVerified()
    confirmVerified(executeSimpleActionUseCase)
  }

  @Test
  fun `should only dismiss action dialog on force action when data is incomplete`() {
    // given
    viewModel.setState(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.overcurrent_question,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = ActionId.TURN_ON,
          remoteId = 123
        )
      )
    )
    states.clear()

    // when
    viewModel.forceAction(null, ActionId.TURN_ON)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    confirmDependenciesVerified()
    confirmVerified(executeSimpleActionUseCase)
  }

  @Test
  fun `should dismiss action dialog`() {
    // given
    viewModel.setState(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.overcurrent_question,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
        )
      )
    )
    states.clear()

    // when
    viewModel.dismissActionDialog()

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    confirmDependenciesVerified()
  }

  @Test
  fun `should perform left button action`() {
    // given
    val remoteId = 123
    every { channelActionUseCase(remoteId, ButtonType.LEFT) } returns Completable.complete()

    // when
    viewModel.onLeftButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { channelActionUseCase(remoteId, ButtonType.LEFT) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should perform right button action`() {
    // given
    val remoteId = 123
    every { channelActionUseCase(remoteId, ButtonType.RIGHT) } returns Completable.complete()

    // when
    viewModel.onRightButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { channelActionUseCase(remoteId, ButtonType.RIGHT) }
    confirmDependenciesVerified()
  }

  @Test
  fun `should reorder channels and reload list when drag stops`() {
    // given
    val remoteId = 123
    val firstItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val channels = listOf<ListItem>(firstItem, secondItem)
    val reorderedChannels = listOf<ListItem>(secondItem, firstItem)
    coEvery { reorderChannelsUseCase(channels, remoteId) } returns Unit
    every { createProfileChannelsListUseCase() } returns Observable.just(reorderedChannels)
    viewModel.setState(ChannelListViewState(channels = channels))
    states.clear()

    // when
    viewModel.onDragStopped(remoteId)

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(channels = reorderedChannels)
    )
    assertThat(events).isEmpty()

    coVerify { reorderChannelsUseCase(channels, remoteId) }
    verify { createProfileChannelsListUseCase() }
    confirmDependenciesVerified()
  }

  @Test
  fun `should show info dialog on info click`() {
    // when
    viewModel.onInfoClick(123)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(ChannelListViewEvent.ShowInfoDialog(123))

    confirmDependenciesVerified()
  }

  @Test
  fun `should show issue dialog on issue click`() {
    // when
    viewModel.onIssueClick("problem")

    // then
    assertThat(states).containsExactly(
      ChannelListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageString = "problem",
          positiveButtonRes = R.string.ok
        )
      )
    )
    assertThat(events).isEmpty()

    confirmDependenciesVerified()
  }

  @Test
  fun `should not allow to process event to fast`() {
    // given
    every { dateProvider.currentTimestamp() } returns 10

    // when
    viewModel.onItemClick(1)

    // then
    confirmVerified(readChannelWithChildrenUseCase)
  }

  private fun confirmDependenciesVerified() {
    confirmVerified(
      createProfileChannelsListUseCase,
      provideChannelDetailTypeUseCase,
      reorderChannelsUseCase,
      toggleLocationUseCase,
      channelActionUseCase
    )
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
      every { this@mockk.profileId } returns this@ChannelListViewModelTest.profileId
      every { this@mockk.channelEntity } returns channelEntity
      every { this@mockk.channelValueEntity } returns channelValueEntity
      every { this@mockk.configEntity } returns configEntity
      every { this@mockk.status } returns status
    }

    return ChannelWithChildren(channel, emptyList())
  }
}
