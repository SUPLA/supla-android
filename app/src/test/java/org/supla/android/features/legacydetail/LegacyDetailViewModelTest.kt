package org.supla.android.features.legacydetail

import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class LegacyDetailViewModelTest : BaseViewModelTest<LegacyDetailViewState, LegacyDetailViewEvent>() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: LegacyDetailViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel data`() {
    // given
    val channelId = 123
    val itemType = ItemType.CHANNEL

    val channel: Channel = mockk()
    whenever(readChannelByRemoteIdUseCase(channelId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.loadData(channelId, itemType)

    // then
    val state = LegacyDetailViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(channel)
    )
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load group data`() {
    // given
    val groupId = 234
    val itemType = ItemType.GROUP

    val group: ChannelGroup = mockk()
    whenever(readChannelGroupByRemoteIdUseCase(groupId)).thenReturn(Maybe.just(group))

    // when
    viewModel.loadData(groupId, itemType)

    // then
    val state = LegacyDetailViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy()
    )
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(group)
    )
    verifyZeroInteractions(readChannelByRemoteIdUseCase)
  }
}
