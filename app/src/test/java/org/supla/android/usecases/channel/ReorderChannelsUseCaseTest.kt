package org.supla.android.usecases.channel

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifyOrder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.core.shared.data.model.general.SuplaFunction

class ReorderChannelsUseCaseTest {

  @MockK
  private lateinit var channelRepository: RoomChannelRepository

  @MockK
  private lateinit var locationRepository: LocationRepository

  private lateinit var useCase: ReorderChannelsUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    useCase = ReorderChannelsUseCase(channelRepository, locationRepository)
  }

  @Test
  fun `should reorder channels and update location sorting`() {
    // given
    val locationId = 21
    val locationCaption = "Living room"
    val location = LocationEntity(
      id = 1L,
      remoteId = locationId,
      caption = locationCaption,
      visible = 1,
      collapsed = 0,
      sorting = LocationSortingType.DEFAULT,
      sortOrder = 1,
      profileId = 7L
    )
    val expectedLocation = location.copy(sorting = LocationSortingType.USER_DEFINED)

    val first = channelDataEntity(channelEntity(id = 11L, remoteId = 101, position = 1))
    val second = channelDataEntity(channelEntity(id = 12L, remoteId = 102, position = 2))
    val third = channelDataEntity(channelEntity(id = 13L, remoteId = 103, position = 3))
    val fourth = channelDataEntity(channelEntity(id = 14L, remoteId = 104, position = 4))
    val expectedSecond = second.channelEntity.copy(position = 1)
    val expectedThird = third.channelEntity.copy(position = 2)
    val expectedFirst = first.channelEntity.copy(position = 3)
    val expectedFourth = fourth.channelEntity.copy(position = 4)

    every { locationRepository.findByRemoteId(locationId) } returns Maybe.just(location)
    every { channelRepository.findChannelsForLocation(locationCaption) } returns Single.just(
      listOf(first, second, third, fourth)
    )
    every { locationRepository.updateLocation(match { it.sorting == LocationSortingType.USER_DEFINED }) } returns Completable.complete()
    every { channelRepository.update(any()) } returns Completable.complete()

    // when
    useCase(firstItemId = 11L, firstItemLocationId = locationId, secondItemId = 13L)
      .test()
      .assertComplete()

    // then
    verifyOrder {
      locationRepository.findByRemoteId(locationId)
      channelRepository.findChannelsForLocation(locationCaption)
      locationRepository.updateLocation(expectedLocation)
      channelRepository.update(expectedSecond)
      channelRepository.update(expectedThird)
      channelRepository.update(expectedFirst)
      channelRepository.update(expectedFourth)
    }
  }

  private fun channelEntity(id: Long, remoteId: Int, position: Int): ChannelEntity =
    ChannelEntity(
      id = id,
      remoteId = remoteId,
      deviceId = null,
      caption = "Channel $remoteId",
      type = 0,
      function = SuplaFunction.UNKNOWN,
      visible = 1,
      locationId = 21,
      altIcon = 0,
      userIcon = 0,
      manufacturerId = 0.toShort(),
      productId = 0.toShort(),
      flags = 0,
      protocolVersion = 0,
      position = position,
      profileId = 7L
    )

  private fun channelDataEntity(channelEntity: ChannelEntity): ChannelDataEntity =
    mockk {
      every { id } returns channelEntity.id
      every { this@mockk.channelEntity } returns channelEntity
    }
}
