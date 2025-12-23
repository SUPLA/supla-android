package org.supla.android.features.details.gpmdetail
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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.StandardDetailViewEvent
import org.supla.android.features.details.detailbase.StandardDetailViewModel
import org.supla.android.features.details.detailbase.StandardDetailViewState
import org.supla.android.testhelpers.extensions.mockShareable
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class StandardDetailViewModelTest : BaseViewModelTest<StandardDetailViewState, StandardDetailViewEvent, StandardDetailViewModel>(
  MockSchedulers.MOCKK
) {

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @MockK
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var preferences: Preferences

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: StandardDetailViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.GENERAL_PURPOSE_METER
    val channelData: ChannelDataEntity = mockk()
    every { channelData.visible } returns 1
    channelData.mockShareable(function = function)
    val caption: LocalizedString = mockk()

    val shareable = channelData.shareable
    every { getCaptionUseCase.invoke(shareable) } returns caption
    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, function)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(StandardDetailViewState(caption))

    verify { readChannelByRemoteIdUseCase.invoke(remoteId) }
    confirmVerified(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should close activity when loaded channel is not visible`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.GENERAL_PURPOSE_METER
    val channelData: ChannelDataEntity = mockk()
    every { channelData.visible } returns 0
    every { channelData.function } returns function
    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, function)

    // then
    Assertions.assertThat(events).containsExactly(StandardDetailViewEvent.Close)
    Assertions.assertThat(states).isEmpty()

    verify { readChannelByRemoteIdUseCase.invoke(remoteId) }
    confirmVerified(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should close activity when loaded channel has different function`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.GENERAL_PURPOSE_METER
    val channelData: ChannelDataEntity = mockk()
    every { channelData.visible } returns 1
    every { channelData.function } returns function
    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channelData)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, SuplaFunction.LIGHTSWITCH)

    // then
    Assertions.assertThat(events).containsExactly(StandardDetailViewEvent.Close)
    Assertions.assertThat(states).isEmpty()

    verify { readChannelByRemoteIdUseCase.invoke(remoteId) }
    confirmVerified(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should reload channel when channels updated`() {
    // given
    val remoteId = 123
    val function = SuplaFunction.DIMMER
    val channelData: ChannelDataEntity = mockk()
    every { channelData.visible } returns 1
    channelData.mockShareable(function = function)
    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channelData)
    every { updateEventsManager.observeChannelEvents(remoteId) } returns Observable.just(UpdateEventsManager.State.Channel)

    val shareable = channelData.shareable
    val caption: LocalizedString = mockk()
    every { getCaptionUseCase.invoke(shareable) } returns caption

    // when
    viewModel.observeUpdates(remoteId, ItemType.CHANNEL, function)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(StandardDetailViewState(caption))

    verify {
      readChannelByRemoteIdUseCase.invoke(remoteId)
      updateEventsManager.observeChannelEvents(remoteId)
    }
    confirmVerified(readChannelByRemoteIdUseCase, updateEventsManager, readChannelGroupByRemoteIdUseCase)
  }
}
