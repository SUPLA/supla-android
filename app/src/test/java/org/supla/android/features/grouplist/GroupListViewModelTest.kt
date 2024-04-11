package org.supla.android.features.grouplist

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions
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
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.details.RollerShutterDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase

@RunWith(MockitoJUnitRunner::class)
class GroupListViewModelTest : BaseViewModelTest<GroupListViewState, GroupListViewEvent, GroupListViewModel>() {

  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var createProfileGroupsListUseCase: CreateProfileGroupsListUseCase

  @Mock
  private lateinit var groupActionUseCase: GroupActionUseCase

  @Mock
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @Mock
  private lateinit var provideDetailTypeUseCase: ProvideDetailTypeUseCase

  @Mock
  private lateinit var findGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: GroupListViewModel by lazy {
    GroupListViewModel(
      channelRepository,
      createProfileGroupsListUseCase,
      groupActionUseCase,
      toggleLocationUseCase,
      provideDetailTypeUseCase,
      findGroupByRemoteIdUseCase,
      loadActiveProfileUrlUseCase,
      updateEventsManager,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(updateEventsManager.observeGroupsUpdate()).thenReturn(listsEventsSubject)
    super.setUp()
  }

  @Test
  fun `should load groups`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadGroups()

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase)
  }

  @Test
  fun `should toggle location collapsed and reload groups`() {
    // given
    val location = mockk<LocationEntity>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.GROUP)).thenReturn(Completable.complete())
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase, toggleLocationUseCase)
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

    whenever(channelRepository.reorderChannelGroups(firstItemId, firstItemLocationId, secondItemId)).thenReturn(Completable.complete())

    // when
    viewModel.swapItems(firstItem, secondItem)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify(channelRepository).reorderChannelGroups(firstItemId, firstItemLocationId, secondItemId)
    verifyNoMoreInteractions(channelRepository)
    verifyZeroInteractionsExcept(channelRepository)
  }

  @Test
  fun `should show valve dialog when action cannot be performed`() {
    // given
    val groupId = 123
    val buttonType = ButtonType.LEFT
    whenever(groupActionUseCase(groupId, buttonType)).thenReturn(Completable.error(ActionException.ChannelClosedManually(groupId)))

    // when
    viewModel.performAction(groupId, buttonType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.ShowValveDialog(groupId)
    )
    verifyZeroInteractionsExcept(groupActionUseCase)
  }

  @Test
  fun `should show exceeded amperage dialog when action cannot be performed`() {
    // given
    val groupId = 123
    val buttonType = ButtonType.RIGHT
    whenever(groupActionUseCase(groupId, buttonType)).thenReturn(Completable.error(ActionException.ChannelExceedAmperage(groupId)))

    // when
    viewModel.performAction(groupId, buttonType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.ShowAmperageExceededDialog(groupId)
    )
    verifyZeroInteractionsExcept(groupActionUseCase)
  }

  @Test
  fun `should not open details when item is offline`() {
    // given
    val remoteId = 123
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.isOnline() } returns false

    whenever(findGroupByRemoteIdUseCase(remoteId)).thenReturn(Maybe.just(groupData))

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open legacy detail fragment`() {
    // given
    val remoteId = 123
    val groupFunction = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.remoteId } returns remoteId
    every { groupData.isOnline() } returns true
    every { groupData.function } returns groupFunction

    val detailType = ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    whenever(provideDetailTypeUseCase(groupData)).thenReturn(detailType)

    whenever(findGroupByRemoteIdUseCase(remoteId)).thenReturn(Maybe.just(groupData))

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val remoteId = 123
    val groupFunction = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.remoteId } returns remoteId
    every { groupData.isOnline() } returns true
    every { groupData.function } returns groupFunction

    whenever(findGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(groupData))

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should open roller shutter detail when item is offline`() {
    // given
    val remoteId = 123
    val function = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
    val groupData: ChannelGroupDataEntity = mockk()
    every { groupData.remoteId } returns remoteId
    every { groupData.function } returns function
    every { groupData.isOnline() } returns false

    val detailType = RollerShutterDetailType(listOf(DetailPage.ROLLER_SHUTTER))
    whenever(provideDetailTypeUseCase(groupData)).thenReturn(detailType)

    whenever(findGroupByRemoteIdUseCase(remoteId)).thenReturn(Maybe.just(groupData))

    // when
    viewModel.onListItemClick(remoteId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.OpenRollerShutterDetail(ItemBundle(remoteId, 0, ItemType.GROUP, function), detailType.pages)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf(mockk<ListItem.ChannelItem>())
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(groups = list)
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase)
  }

  @Test
  fun `should load group on update`() {
    // given
    val groupId = 223
    val group: ChannelGroupDataEntity = mockk()
    every { group.remoteId } returns groupId
    whenever(findGroupByRemoteIdUseCase(groupId)).thenReturn(Maybe.just(group))

    val suplaMessage: SuplaClientMsg = mockk()
    every { suplaMessage.channelGroupId } returns groupId
    every { suplaMessage.type } returns SuplaClientMsg.onDataChanged

    val list = listOf(mockk<ListItem.ChannelItem>())
    every { list[0].channelBase } returns group
    every { list[0].channelBase = group } answers { }
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadGroups()
    viewModel.onSuplaMessage(suplaMessage)

    // then
    Assertions.assertThat(states).containsExactly(GroupListViewState(groups = list))
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(findGroupByRemoteIdUseCase, createProfileGroupsListUseCase)
    io.mockk.verify { list[0].channelBase = group }
  }

  @Test
  fun `should do nothing when update is not for group`() {
    // given
    val suplaMessage: SuplaClientMsg = mockk()
    every { suplaMessage.channelGroupId } returns 0
    every { suplaMessage.type } returns SuplaClientMsg.onDataChanged

    // when
    viewModel.onSuplaMessage(suplaMessage)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `on add group click should open supla cloud`() {
    // given
    whenever(loadActiveProfileUrlUseCase.invoke()).thenReturn(Single.just(CloudUrl.SuplaCloud))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(GroupListViewEvent.NavigateToSuplaCloud)
    verify(loadActiveProfileUrlUseCase).invoke()
    verifyNoMoreInteractions(loadActiveProfileUrlUseCase)
    verifyZeroInteractionsExcept(loadActiveProfileUrlUseCase)
  }

  @Test
  fun `on add group click should open private cloud`() {
    // given
    val url: Uri = mockk()
    whenever(loadActiveProfileUrlUseCase.invoke()).thenReturn(Single.just(CloudUrl.PrivateCloud(url)))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(GroupListViewEvent.NavigateToPrivateCloud(url))
    verify(loadActiveProfileUrlUseCase).invoke()
    verifyNoMoreInteractions(loadActiveProfileUrlUseCase)
    verifyZeroInteractionsExcept(loadActiveProfileUrlUseCase)
  }

  private fun verifyZeroInteractionsExcept(vararg except: Any) {
    val allDependencies = listOf(
      channelRepository,
      createProfileGroupsListUseCase,
      groupActionUseCase,
      toggleLocationUseCase,
      provideDetailTypeUseCase,
      loadActiveProfileUrlUseCase
    )
    for (dependency in allDependencies) {
      if (!except.contains(dependency)) {
        verifyZeroInteractions(dependency)
      }
    }
  }
}
