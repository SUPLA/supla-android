package org.supla.android.features.grouplist

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.Location
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase

@RunWith(MockitoJUnitRunner::class)
class GroupListViewModelTest : BaseViewModelTest<GroupListViewState, GroupListViewEvent>() {

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
  private lateinit var listsEventsManager: ListsEventsManager

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
      listsEventsManager,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(listsEventsManager.observeGroupUpdates()).thenReturn(listsEventsSubject)
    super.setUp()
  }

  @Test
  fun `should load groups`() {
    // given
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadGroups()

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, groups = list),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase)
  }

  @Test
  fun `should toggle location collapsed and reload groups`() {
    // given
    val location = mockk<Location>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.GROUP)).thenReturn(Completable.complete())
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, groups = list),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase, toggleLocationUseCase)
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

    whenever(channelRepository.reorderChannelGroups(firstItemId, firstItemLocationId, secondItemId)).thenReturn(Completable.complete())

    // when
    viewModel.swapItems(firstItem, secondItem)

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
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
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
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
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.ShowAmperageExceededDialog(groupId)
    )
    verifyZeroInteractionsExcept(groupActionUseCase)
  }

  @Test
  fun `should not open details when item is offline`() {
    // given
    val group = mockk<ChannelGroup>()
    every { group.onLine } returns false

    // when
    viewModel.onListItemClick(group)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open thermostat details for channel with thermostat function`() {
    // given
    val group = mockk<ChannelGroup>()
    every { group.onLine } returns true
    every { group.func } returns SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT

    // when
    viewModel.onListItemClick(group)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.OpenThermostatDetails
    )
    verifyZeroInteractionsExcept()
  }

  @Test
  fun `should open legacy detail fragment`() {
    // given
    val groupId = 123
    val groupFunction = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val group = mockk<ChannelGroup>()
    every { group.onLine } returns true
    every { group.func } returns groupFunction
    every { group.groupId } returns groupId

    val legacyDetailType = LegacyDetailType.TEMPERATURE
    whenever(provideDetailTypeUseCase(group)).thenReturn(legacyDetailType)

    // when
    viewModel.onListItemClick(group)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.OpenLegacyDetails(groupId, legacyDetailType)
    )
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should not open detail fragment when it is not supported`() {
    // given
    val groupId = 123
    val groupFunction = SuplaConst.SUPLA_CHANNELFNC_NONE
    val group = mockk<ChannelGroup>()
    every { group.onLine } returns true
    every { group.func } returns groupFunction
    every { group.groupId } returns groupId

    // when
    viewModel.onListItemClick(group)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(provideDetailTypeUseCase)
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileGroupsListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = GroupListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, groups = list),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileGroupsListUseCase)
  }

  @Test
  fun `should load group on update`() {
    // given
    val groupId = 223
    val group: ChannelGroup = mockk()

    whenever(findGroupByRemoteIdUseCase(groupId)).thenReturn(Maybe.just(group))

    // when
    viewModel.onGroupUpdate(groupId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      GroupListViewEvent.UpdateGroup(group)
    )
    verifyZeroInteractionsExcept(findGroupByRemoteIdUseCase)
  }

  @Test
  fun `should do nothing when group not found on update`() {
    // given
    val groupId = 223

    whenever(findGroupByRemoteIdUseCase(groupId)).thenReturn(Maybe.empty())

    // when
    viewModel.onGroupUpdate(groupId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(findGroupByRemoteIdUseCase)
  }

  private fun verifyZeroInteractionsExcept(vararg except: Any) {
    val allDependencies = listOf(
      channelRepository,
      createProfileGroupsListUseCase,
      groupActionUseCase,
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
