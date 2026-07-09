package org.supla.android.usecases.captionchange
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
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.events.UpdateEventsManager
import org.supla.android.usecases.captionchange.CaptionChangeUseCase.Type

class CaptionChangeUseCaseTest {

  @MockK
  private lateinit var locationRepository: LocationRepository

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var groupRepository: ChannelGroupRepository

  @MockK
  private lateinit var sceneRepository: SceneRepository

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @InjectMockKs
  private lateinit var useCase: CaptionChangeUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    every { updateEventsManager.emitChannelUpdate(any()) } just Runs
    every { updateEventsManager.emitGroupUpdate(any()) } just Runs
    every { updateEventsManager.emitSceneUpdate(any()) } just Runs
  }

  @Test
  fun `should update location caption locally and remotely`() {
    // given
    val caption = "Kitchen"
    val remoteId = 11
    val profileId = 22L
    val suplaClient: SuplaClientApi = io.mockk.mockk {
      every { setLocationCaption(remoteId, caption) } returns true
    }
    every { locationRepository.updateCaption(caption, remoteId, profileId) } returns Completable.complete()
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase(caption, Type.LOCATION, remoteId, profileId).test()

    // then
    observer.assertComplete()
    verify {
      locationRepository.updateCaption(caption, remoteId, profileId)
      suplaClientProvider.provide()
      suplaClient.setLocationCaption(remoteId, caption)
    }
    confirmVerified(
      locationRepository,
      suplaClientProvider,
      suplaClient,
      channelRepository,
      groupRepository,
      sceneRepository,
      updateEventsManager
    )
  }

  @Test
  fun `should update channel caption and emit channel update`() {
    // given
    val caption = "Light"
    val remoteId = 12
    val profileId = 23L
    val suplaClient: SuplaClientApi = io.mockk.mockk {
      every { setChannelCaption(remoteId, caption) } returns true
    }
    every { channelRepository.updateCaption(caption, remoteId, profileId) } returns Completable.complete()
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase(caption, Type.CHANNEL, remoteId, profileId).test()

    // then
    observer.assertComplete()
    verify {
      channelRepository.updateCaption(caption, remoteId, profileId)
      suplaClientProvider.provide()
      suplaClient.setChannelCaption(remoteId, caption)
      updateEventsManager.emitChannelUpdate(remoteId)
    }
    confirmVerified(
      channelRepository,
      suplaClientProvider,
      suplaClient,
      updateEventsManager,
      locationRepository,
      groupRepository,
      sceneRepository
    )
  }

  @Test
  fun `should update group caption and emit group update`() {
    // given
    val caption = "Living room"
    val remoteId = 13
    val profileId = 24L
    val suplaClient: SuplaClientApi = io.mockk.mockk {
      every { setChannelGroupCaption(remoteId, caption) } returns true
    }
    every { groupRepository.updateCaption(caption, remoteId, profileId) } returns Completable.complete()
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase(caption, Type.GROUP, remoteId, profileId).test()

    // then
    observer.assertComplete()
    verify {
      groupRepository.updateCaption(caption, remoteId, profileId)
      suplaClientProvider.provide()
      suplaClient.setChannelGroupCaption(remoteId, caption)
      updateEventsManager.emitGroupUpdate(remoteId)
    }
    confirmVerified(
      groupRepository,
      suplaClientProvider,
      suplaClient,
      updateEventsManager,
      locationRepository,
      channelRepository,
      sceneRepository
    )
  }

  @Test
  fun `should update scene caption and emit scene update`() {
    // given
    val caption = "Night scene"
    val remoteId = 14
    val profileId = 25L
    val suplaClient: SuplaClientApi = io.mockk.mockk {
      every { setSceneCaption(remoteId, caption) } returns true
    }
    every { sceneRepository.updateCaption(caption, remoteId, profileId) } returns Completable.complete()
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    val observer = useCase(caption, Type.SCENE, remoteId, profileId).test()

    // then
    observer.assertComplete()
    verify {
      sceneRepository.updateCaption(caption, remoteId, profileId)
      suplaClientProvider.provide()
      suplaClient.setSceneCaption(remoteId, caption)
      updateEventsManager.emitSceneUpdate(remoteId)
    }
    confirmVerified(
      sceneRepository,
      suplaClientProvider,
      suplaClient,
      updateEventsManager,
      locationRepository,
      channelRepository,
      groupRepository
    )
  }

  @Test
  fun `should only update repository when supla client is unavailable`() {
    // given
    val caption = "Garage"
    val remoteId = 15
    val profileId = 26L
    every { channelRepository.updateCaption(caption, remoteId, profileId) } returns Completable.complete()
    every { suplaClientProvider.provide() } returns null

    // when
    val observer = useCase(caption, Type.CHANNEL, remoteId, profileId).test()

    // then
    observer.assertComplete()
    verify {
      channelRepository.updateCaption(caption, remoteId, profileId)
      suplaClientProvider.provide()
    }
    confirmVerified(
      channelRepository,
      suplaClientProvider,
      locationRepository,
      groupRepository,
      sceneRepository,
      updateEventsManager
    )
  }
}
