package org.supla.android.features.grouplist

import android.net.Uri
import io.mockk.MockKAnnotations
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
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.ActionAlertDialogState
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.GroupActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.HumidityDetailType
import org.supla.android.usecases.details.ProvideGroupDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

class GroupListViewModelTest : BaseViewModelTest<GroupListViewState, GroupListViewEvent, GroupListViewModel>(MockSchedulers.MOCKK) {

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var createProfileGroupsListUseCase: CreateProfileGroupsListUseCase

  @MockK
  private lateinit var groupActionUseCase: GroupActionUseCase

  @MockK
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @MockK
  private lateinit var provideGroupDetailTypeUseCase: ProvideGroupDetailTypeUseCase

  @MockK
  private lateinit var findGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK(relaxed = true)
  private lateinit var preferences: Preferences

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK(relaxed = true)
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: GroupListViewModel by lazy {
    GroupListViewModel(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      findGroupByRemoteIdUseCase,
      executeSimpleActionUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      channelRepository,
      profileRepository,
      loadActiveProfileUrlUseCase,
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
    every { updateEventsManager.observeGroupsUpdate() } returns listsEventsSubject

    super.setUp()
  }

  @Test
  fun `should load groups`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should toggle location collapsed and reload groups`() {
    // given
    val location = mockk<LocationEntity>()
    every { toggleLocationUseCase(location, CollapsedFlag.GROUP) } returns Completable.complete()
    val list = listOf(mockk<ListItem.ChannelItem>())
    every { createProfileGroupsListUseCase() } returns Observable.just(list)

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()

    verify {
      createProfileGroupsListUseCase.invoke()
      toggleLocationUseCase.invoke(location, CollapsedFlag.GROUP)
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should swap when items are not null`() {
    // given
    val firstItemId = 123L
    val firstItemLocationId = 234
    val firstItem = mockk<ChannelDataBase>()
    val profileId = 1L
    every { firstItem.id } returns firstItemId
    every { firstItem.locationId } returns firstItemLocationId

    val secondItemId = 345L
    val secondItem = mockk<ChannelDataBase>()
    every { secondItem.id } returns secondItemId

    val profile: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { profileRepository.findActiveProfile() } returns Single.just(profile)
    every { channelRepository.reorderChannelGroups(firstItemId, firstItemLocationId, secondItemId, profileId) } returns
      Completable.complete()

    // when
    viewModel.swapItems(firstItem, secondItem)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify {
      profileRepository.findActiveProfile()
      channelRepository.reorderChannelGroups(firstItemId, firstItemLocationId, secondItemId, profileId)
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should open legacy detail fragment`() {
    // given
    val remoteId = 123
    val groupFunction = SuplaFunction.THERMOMETER
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.remoteId } returns remoteId
    every { groupData.status } returns SuplaChannelAvailabilityStatus.ONLINE
    every { groupData.function } returns groupFunction

    val detailType = ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    every { provideGroupDetailTypeUseCase(groupData) } returns detailType

    every { findGroupByRemoteIdUseCase(remoteId) } returns Maybe.just(groupData)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val remoteId = 123
    val groupFunction = SuplaFunction.THERMOMETER
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.remoteId } returns remoteId
    every { groupData.status } returns SuplaChannelAvailabilityStatus.ONLINE
    every { groupData.function } returns groupFunction

    every { provideGroupDetailTypeUseCase.invoke(groupData) } returns HumidityDetailType(emptyList())
    every { findGroupByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(groupData)
    every { dateProvider.currentTimestamp() } returns 500

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

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
      profileRepository,
      channelRepository,
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
      GroupListViewEvent.OpenStandardDetail(ItemBundle(remoteId, 0, profileId, ItemType.GROUP, function), detailType.pages)
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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }

  @Test
  fun `should load group on update`() {
    // given
    val groupId = 223
    val group: ChannelGroupDataEntity = mockk()
    every { group.remoteId } returns groupId
    every { findGroupByRemoteIdUseCase(groupId) } returns Maybe.just(group)

    val list = listOf(mockk<ListItem.ChannelItem>())
    every { list[0].channelBase } returns group
    every { list[0].channelBase = group } answers { }
    every { createProfileGroupsListUseCase() } returns Observable.just(list)

    // when
    viewModel.loadGroups()
    viewModel.updateGroup(groupId)

    // then
    Assertions.assertThat(states).containsExactly(GroupListViewState(groups = list))
    Assertions.assertThat(events).isEmpty()

    verify {
      list[0].channelBase = group
      findGroupByRemoteIdUseCase.invoke(groupId)
      createProfileGroupsListUseCase.invoke()
    }
    confirmVerified(
      createProfileGroupsListUseCase,
      provideGroupDetailTypeUseCase,
      loadActiveProfileUrlUseCase,
      findGroupByRemoteIdUseCase,
      toggleLocationUseCase,
      groupActionUseCase,
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
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
      profileRepository,
      channelRepository,
      dateProvider
    )
  }
}
