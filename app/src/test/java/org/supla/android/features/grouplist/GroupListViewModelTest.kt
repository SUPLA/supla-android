package org.supla.android.features.grouplist
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

import android.net.Uri
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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.ActionAlertDialogState
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.GroupActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.ProvideGroupDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.GroupToListItemMapper
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReorderGroupsUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

class GroupListViewModelTest : BaseViewModelTest<GroupListViewState, GroupListViewEvent, GroupListViewModel>(MockSchedulers.MOCKK) {
  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var createProfileGroupsListUseCase: CreateProfileGroupsListUseCase

  @MockK
  private lateinit var groupActionUseCase: GroupActionUseCase

  @MockK
  private lateinit var groupToListItemMapper: GroupToListItemMapper

  @MockK
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @MockK
  private lateinit var provideGroupDetailTypeUseCase: ProvideGroupDetailTypeUseCase

  @MockK
  private lateinit var findGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @MockK
  private lateinit var reorderGroupsUseCase: ReorderGroupsUseCase

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK(relaxed = true)
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: GroupListViewModel by lazy {
    GroupListViewModel(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      findGroupByRemoteIdUseCase,
      executeSimpleActionUseCase,
      groupToListItemMapper,
      toggleLocationUseCase,
      reorderGroupsUseCase,
      groupActionUseCase,
      loadActiveProfileUrlUseCase,
      updateEventsManager,
      dateProvider,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { updateEventsManager.observeGroupsUpdate() } returns listsEventsSubject
    every { updateEventsManager.observeAllGroups() } returns Observable.empty()

    super.setUp()
  }

  @Test
  fun `should load groups`() {
    // given
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileGroupsListUseCase.invoke() } returns Observable.just(list)

    // when
    viewModel.loadGroups()

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      createProfileGroupsListUseCase.invoke()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should toggle location collapsed and reload groups`() {
    // given
    val locationId = 123
    every { toggleLocationUseCase(locationId, CollapsedFlag.GROUP) } returns Completable.complete()
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileGroupsListUseCase() } returns Observable.just(list)

    // when
    viewModel.onLocationClick(locationId)

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      createProfileGroupsListUseCase.invoke()
      toggleLocationUseCase.invoke(locationId, CollapsedFlag.GROUP)
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should swap when items are not null`() {
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
    viewModel.setState(GroupListViewState(groups = items))

    // when
    viewModel.moveItems(0, 2)

    // then
    assertThat(states).containsExactly(
      GroupListViewState(groups = listOf(firstItem, secondItem, thirdItem)),
      GroupListViewState(groups = listOf(secondItem, thirdItem, firstItem))
    )
    Assertions.assertThat(events).isEmpty()

    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
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
    viewModel.setState(GroupListViewState(groups = items))

    // when
    viewModel.moveItems(0, 2)

    // then
    assertThat(states).containsExactly(
      GroupListViewState(groups = listOf(firstItem, secondItem, thirdItem)),
    )
    Assertions.assertThat(events).isEmpty()

    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
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
    viewModel.setState(GroupListViewState(groups = items))

    // when
    viewModel.moveItems(2, 0)

    // then
    assertThat(states).containsExactly(
      GroupListViewState(groups = listOf(firstItem, secondItem, thirdItem)),
    )
    Assertions.assertThat(events).isEmpty()

    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should show valve dialog when action cannot be performed`() {
    // given
    val groupId = 123
    val buttonType = ButtonType.LEFT
    every { groupActionUseCase(groupId, buttonType) } returns Completable.error(ActionException.ValveClosedManually(groupId))

    // when
    viewModel.performAction(groupId, buttonType)

    // then
    Assertions.assertThat(states).containsExactly(
      GroupListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.valve_warning_manually_closed,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = ActionId.OPEN,
          remoteId = groupId
        )
      )
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      groupActionUseCase(groupId, buttonType)
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should show exceeded amperage dialog when action cannot be performed`() {
    // given
    val groupId = 123
    val buttonType = ButtonType.RIGHT
    every { groupActionUseCase(groupId, buttonType) } returns Completable.error(ActionException.ChannelExceedAmperage(groupId))

    // when
    viewModel.performAction(groupId, buttonType)

    // then
    Assertions.assertThat(states).containsExactly(
      GroupListViewState(
        actionAlertDialogState = ActionAlertDialogState(
          messageRes = R.string.overcurrent_question,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          actionId = ActionId.TURN_ON,
          remoteId = groupId
        )
      )
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      groupActionUseCase(groupId, buttonType)
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should not open details when item is offline`() {
    // given
    val remoteId = 123
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.status } returns SuplaChannelAvailabilityStatus.OFFLINE

    every { findGroupByRemoteIdUseCase(remoteId) } returns Maybe.just(groupData)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify {
      findGroupByRemoteIdUseCase.invoke(remoteId)
      dateProvider.currentTimestamp()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should open roller shutter detail when item is offline`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
    val groupData: ChannelGroupDataEntity = mockk()
    val profileId = 1L
    every { groupData.remoteId } returns remoteId
    every { groupData.function } returns function
    every { groupData.profileId } returns profileId
    every { groupData.status } returns SuplaChannelAvailabilityStatus.OFFLINE

    val detailType = StandardDetailType(listOf(DetailPage.ROLLER_SHUTTER))
    every { provideGroupDetailTypeUseCase(groupData) } returns detailType

    every { findGroupByRemoteIdUseCase(remoteId) } returns Maybe.just(groupData)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.OpenDetail(ItemBundle(remoteId, 0, profileId, ItemType.GROUP, function), detailType.pages)
    )

    verify {
      provideGroupDetailTypeUseCase.invoke(groupData)
      findGroupByRemoteIdUseCase.invoke(remoteId)
      dateProvider.currentTimestamp()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileGroupsListUseCase.invoke() } returns Observable.just(list)

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      createProfileGroupsListUseCase.invoke()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should set filter text and reload groups`() {
    // given
    val filterText = "kitchen"
    val list = listOf(mockk<ListItem.DefaultItem>())
    every { createProfileGroupsListUseCase(filterText) } returns Observable.just(list)

    // when
    viewModel.handle(TopBarSearchEvent.QueryChange(filterText))

    // then
    assertThat(viewModel.searchData.query).isEqualTo(filterText)
    assertThat(states).containsExactly(
      GroupListViewState(groups = list)
    )
    assertThat(events).isEmpty()

    verify { createProfileGroupsListUseCase(filterText) }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should dismiss action dialog on force action and execute requested action`() {
    // given
    val remoteId = 123
    val actionId = ActionId.TURN_ON
    every { executeSimpleActionUseCase(actionId, SubjectType.GROUP, remoteId) } returns Completable.complete()
    viewModel.setState(
      GroupListViewState(
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
      GroupListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    verify { executeSimpleActionUseCase(actionId, SubjectType.GROUP, remoteId) }
    confirmVerified(executeSimpleActionUseCase)
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should only dismiss action dialog on force action when data is incomplete`() {
    // given
    viewModel.setState(
      GroupListViewState(
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
      GroupListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    confirmVerified(executeSimpleActionUseCase)
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should dismiss action dialog`() {
    // given
    viewModel.setState(
      GroupListViewState(
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
      GroupListViewState(actionAlertDialogState = null)
    )
    assertThat(events).isEmpty()

    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      executeSimpleActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should perform left button action`() {
    // given
    val remoteId = 123
    every { groupActionUseCase(remoteId, ButtonType.LEFT) } returns Completable.complete()

    // when
    viewModel.onLeftButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { groupActionUseCase(remoteId, ButtonType.LEFT) }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should perform right button action`() {
    // given
    val remoteId = 123
    every { groupActionUseCase(remoteId, ButtonType.RIGHT) } returns Completable.complete()

    // when
    viewModel.onRightButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { groupActionUseCase(remoteId, ButtonType.RIGHT) }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `should reorder groups and reload list when drag stops`() {
    // given
    val remoteId = 123
    val firstItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.DefaultItem = mockk {
      every { locationCaption } returns "1"
    }
    val groups = listOf<ListItem>(firstItem, secondItem)
    val reorderedGroups = listOf<ListItem>(secondItem, firstItem)
    coEvery { reorderGroupsUseCase(groups, remoteId) } returns Unit
    every { createProfileGroupsListUseCase() } returns Observable.just(reorderedGroups)
    viewModel.setState(GroupListViewState(groups = groups))
    states.clear()

    // when
    viewModel.onDragStopped(remoteId)

    // then
    assertThat(states).containsExactly(
      GroupListViewState(groups = reorderedGroups)
    )
    assertThat(events).isEmpty()

    coVerify { reorderGroupsUseCase(groups, remoteId) }
    verify { createProfileGroupsListUseCase() }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      reorderGroupsUseCase,
      dateProvider
    )
  }

  @Test
  fun `on add group click should open supla cloud`() {
    // given
    every { loadActiveProfileUrlUseCase.invoke() } returns Single.just(CloudUrl.DefaultCloud)

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(GroupListViewEvent.NavigateToSuplaCloud)

    verify {
      loadActiveProfileUrlUseCase.invoke()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `on add group click should open private cloud`() {
    // given
    val url: Uri = mockk()
    every { loadActiveProfileUrlUseCase.invoke() } returns Single.just(CloudUrl.ServerUri(url))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(GroupListViewEvent.NavigateToPrivateCloud(url))
    verify {
      loadActiveProfileUrlUseCase.invoke()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }

  @Test
  fun `should not allow to process event to fast`() {
    // given
    every { dateProvider.currentTimestamp() } returns 10

    // when
    viewModel.onListItemClick(1)

    // then
    verify {
      dateProvider.currentTimestamp()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      dateProvider
    )
  }
}
