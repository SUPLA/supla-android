package org.supla.android.usecases.group

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.lib.SuplaConst

class ReadGroupTiltingDetailsUseCaseTest {

  @MockK
  private lateinit var groupRelationRepository: ChannelGroupRelationRepository

  @MockK
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @InjectMockKs
  private lateinit var useCase: ReadGroupTiltingDetailsUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get unknown when group is empty`() {
    // given
    val remoteId = 123
    every { groupRelationRepository.findGroupRelations(remoteId) } returns Single.just(emptyList())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    observer.assertValues(TiltingDetails.Unknown)

    verify {
      groupRelationRepository.findGroupRelations(remoteId)
      channelConfigRepository wasNot Called
    }
  }

  @Test
  fun `should get unknown when config not supported`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val function = SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND
    val relation = mockRelation(profileId, remoteId, function)
    every { groupRelationRepository.findGroupRelations(remoteId) } returns Single.just(listOf(relation))
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.FACADE_BLIND) } returns Single.just(mockk())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    observer.assertValues(TiltingDetails.Unknown)

    verify {
      groupRelationRepository.findGroupRelations(remoteId)
      channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.FACADE_BLIND)
    }
    confirmVerified(groupRelationRepository, channelConfigRepository)
  }

  @Test
  fun `should get similar when both configs are similar`() {
    // given
    val profileId = 11L
    val groupId = 321
    val function = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
    val relation1 = mockRelation(profileId, 123, function)
    val relation2 = mockRelation(profileId, 234, function)
    val config = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    every { groupRelationRepository.findGroupRelations(groupId) } returns Single.just(listOf(relation1, relation2))
    every {
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config)
    every {
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config)

    // when
    val observer = useCase.invoke(groupId).test()

    // then
    observer.assertComplete()
    observer.assertValues(TiltingDetails.Similar(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING))

    verify {
      groupRelationRepository.findGroupRelations(groupId)
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
    }
    confirmVerified(groupRelationRepository, channelConfigRepository)
  }

  @Test
  fun `should get different when configs are different`() {
    // given
    val profileId = 11L
    val groupId = 321
    val function = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
    val relation1 = mockRelation(profileId, 123, function)
    val relation2 = mockRelation(profileId, 234, function)
    val config1 = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)
    val config2 = mockConfig(0, 180, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    every { groupRelationRepository.findGroupRelations(groupId) } returns Single.just(listOf(relation1, relation2))
    every {
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config1)
    every {
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config2)

    // when
    val observer = useCase.invoke(groupId).test()

    // then
    observer.assertComplete()
    observer.assertValues(TiltingDetails.Different)

    verify {
      groupRelationRepository.findGroupRelations(groupId)
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
    }
    confirmVerified(groupRelationRepository, channelConfigRepository)
  }

  @Test
  fun `should get different when first configs are different and then comes third one - similar`() {
    // given
    val profileId = 11L
    val groupId = 321
    val function = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
    val relation1 = mockRelation(profileId, 123, function)
    val relation2 = mockRelation(profileId, 234, function)
    val relation3 = mockRelation(profileId, 345, function)
    val config1 = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)
    val config2 = mockConfig(0, 180, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    every { groupRelationRepository.findGroupRelations(groupId) } returns Single.just(listOf(relation1, relation2, relation3))
    every {
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config1)
    every {
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config2)
    every {
      channelConfigRepository.findChannelConfig(profileId, 345, ChannelConfigType.FACADE_BLIND)
    } returns Single.just(config2)

    // when
    val observer = useCase.invoke(groupId).test()

    // then
    observer.assertComplete()
    observer.assertValues(TiltingDetails.Different)

    verify {
      groupRelationRepository.findGroupRelations(groupId)
      channelConfigRepository.findChannelConfig(profileId, 123, ChannelConfigType.FACADE_BLIND)
      channelConfigRepository.findChannelConfig(profileId, 234, ChannelConfigType.FACADE_BLIND)
      channelConfigRepository.findChannelConfig(profileId, 345, ChannelConfigType.FACADE_BLIND)
    }
    confirmVerified(groupRelationRepository, channelConfigRepository)
  }

  private fun mockRelation(profileId: Long, remoteId: Int, function: Int): ChannelGroupRelationDataEntity {
    val channel: ChannelEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.profileId } returns profileId
      every { this@mockk.function } returns function
    }
    return mockk {
      every { channelEntity } returns channel
    }
  }

  private fun mockConfig(tilt0: Int, tilt100: Int, type: SuplaTiltControlType) =
    SuplaChannelFacadeBlindConfig(
      remoteId = 0,
      func = 0,
      crc32 = 0L,
      closingTimeMs = 0,
      openingTimeMs = 0,
      tiltingTimeMs = 0,
      motorUpsideDown = false,
      buttonsUpsideDown = false,
      timeMargin = 0,
      tilt0Angle = tilt0,
      tilt100Angle = tilt100,
      type = type
    )
}
