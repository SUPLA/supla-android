package org.supla.android.usecases.channelconfig

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.lib.SuplaChannel
import org.supla.android.lib.SuplaConst

class RequestChannelConfigUseCaseTest {

  @MockK
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var suplaClient: SuplaClientApi

  @InjectMockKs
  private lateinit var useCase: RequestChannelConfigUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)

    every { suplaClientProvider.provide() } returns suplaClient
  }

  @Test
  fun `should just quit when config should not be requested`() {
    // given
    val suplaChannel = SuplaChannel()
    suplaChannel.Func = SuplaConst.SUPLA_CHANNELFNC_NONE

    // when
    val observer = useCase.invoke(suplaChannel).test()

    // then
    observer.assertComplete()
    verify {
      channelConfigRepository wasNot Called
      suplaClientProvider wasNot Called
    }
  }

  @Test
  fun `should ask for config when crc differs`() {
    // given
    val remoteId = 123
    val config: ChannelConfigEntity = mockk {
      every { configCrc32 } returns 10L
    }
    val suplaChannel = SuplaChannel()
    suplaChannel.Id = remoteId
    suplaChannel.Func = SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
    suplaChannel.DefaultConfigCRC32 = 5L
    every { channelConfigRepository.findForRemoteId(remoteId) } returns Maybe.just(config)
    every { suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT) } returns true

    // when
    val observer = useCase.invoke(suplaChannel).test()

    // then
    observer.assertComplete()
    verify {
      channelConfigRepository.findForRemoteId(remoteId)
      suplaClientProvider.provide()
      suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
    }
    confirmVerified(channelConfigRepository, suplaClientProvider, suplaClient)
  }

  @Test
  fun `should not ask for config when crc same`() {
    // given
    val remoteId = 123
    val config: ChannelConfigEntity = mockk {
      every { configCrc32 } returns 10L
    }
    val suplaChannel = SuplaChannel()
    suplaChannel.Id = remoteId
    suplaChannel.Func = SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
    suplaChannel.DefaultConfigCRC32 = 10L
    every { channelConfigRepository.findForRemoteId(remoteId) } returns Maybe.just(config)

    // when
    val observer = useCase.invoke(suplaChannel).test()

    // then
    observer.assertComplete()
    verify {
      channelConfigRepository.findForRemoteId(remoteId)
    }
    confirmVerified(channelConfigRepository, suplaClientProvider, suplaClient)
  }

  @Test
  fun `should ask for config when config not stored`() {
    // given
    val remoteId = 123
    val suplaChannel = SuplaChannel()
    suplaChannel.Id = remoteId
    suplaChannel.Func = SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
    suplaChannel.DefaultConfigCRC32 = 10L
    every { channelConfigRepository.findForRemoteId(remoteId) } returns Maybe.error(NoSuchElementException())
    every { suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT) } returns true

    // when
    val observer = useCase.invoke(suplaChannel).test()

    // then
    observer.assertComplete()
    verify {
      channelConfigRepository.findForRemoteId(remoteId)
      suplaClientProvider.provide()
      suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
    }
    confirmVerified(channelConfigRepository, suplaClientProvider, suplaClient)
  }
}
