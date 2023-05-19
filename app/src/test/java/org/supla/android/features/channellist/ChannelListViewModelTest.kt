package org.supla.android.features.channellist

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
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
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaConst.*
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.DetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase

@RunWith(MockitoJUnitRunner::class)
class ChannelListViewModelTest : BaseViewModelTest<ChannelListViewState, ChannelListViewEvent>() {

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
  private lateinit var listsEventsManager: ListsEventsManager

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
      listsEventsManager,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(listsEventsManager.observeChannelUpdates()).thenReturn(listsEventsSubject)
    super.setUp()
  }

  @Test
  fun `should load channels`() {
    // given
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.loadChannels()

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, channels = list),
      state.copy()
    )
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase)
  }

  @Test
  fun `should toggle location collapsed and reload channels`() {
    // given
    val location = mockk<Location>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.CHANNEL)).thenReturn(Completable.complete())
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, channels = list),
      state.copy()
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
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
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
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
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
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
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

    val detailType = DetailType.EM
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
    val channelFunction = SUPLA_CHANNELFNC_THERMOMETER
    val channel = mockk<Channel>()
    every { channel.onLine } returns true
    every { channel.func } returns channelFunction
    every { channel.channelId } returns channelId

    val detailType = DetailType.TEMPERATURE
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
    val list = emptyList<ListItem.ChannelItem>()
    whenever(createProfileChannelsListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = ChannelListViewState()
    assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, channels = list),
      state.copy()
    )
    assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileChannelsListUseCase)
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
