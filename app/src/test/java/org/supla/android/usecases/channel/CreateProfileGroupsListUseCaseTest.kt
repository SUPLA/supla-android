package org.supla.android.usecases.channel

import android.database.Cursor
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Location
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag

@RunWith(MockitoJUnitRunner::class)
class CreateProfileGroupsListUseCaseTest {
  @Mock
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @InjectMocks
  private lateinit var usecase: CreateProfileGroupsListUseCase

  @Test
  fun `should create list of channels and locations`() {
    // given
    val firstLocationId = 1
    val collapsedLocationId = 2
    val thirdLocationId = 3

    val firstGroup = mockGroupData(11, firstLocationId, "Location")
    val secondGroup = mockGroupData(22, firstLocationId, "Location")
    val thirdGroup = mockGroupData(33, collapsedLocationId, "Collapsed location", true)
    val fourthGroup = mockGroupData(44, thirdLocationId)

    whenever(channelGroupRepository.findList()).thenReturn(Single.just(listOf(firstGroup, secondGroup, thirdGroup, fourthGroup)))

    // when
    val testObserver = usecase.invoke().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.ChannelItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.ChannelItem::class.java)

    Assertions.assertThat((list[1] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(11)
    Assertions.assertThat((list[2] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(22)
    Assertions.assertThat((list[5] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(44)

    Assertions.assertThat((list[0] as ListItem.LocationItem).location.remoteId).isEqualTo(firstLocationId)
    Assertions.assertThat((list[3] as ListItem.LocationItem).location.remoteId).isEqualTo(collapsedLocationId)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location.remoteId).isEqualTo(thirdLocationId)
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val firstLocationId = 1
    val secondLocationId = 2
    val thirdLocationId = 3

    val firstGroup = mockGroupData(11, firstLocationId, "Location")
    val secondGroup = mockGroupData(22, firstLocationId, "Location")
    val thirdGroup = mockGroupData(33, secondLocationId, "Location")
    val fourthGroup = mockGroupData(44, thirdLocationId)

    whenever(channelGroupRepository.findList()).thenReturn(Single.just(listOf(firstGroup, secondGroup, thirdGroup, fourthGroup)))

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.ChannelItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.ChannelItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.ChannelItem::class.java)

    Assertions.assertThat((list[1] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(11)
    Assertions.assertThat((list[2] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(22)
    Assertions.assertThat((list[3] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(33)
    Assertions.assertThat((list[5] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(44)

    Assertions.assertThat((list[0] as ListItem.LocationItem).location.remoteId).isEqualTo(firstLocationId)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location.remoteId).isEqualTo(thirdLocationId)
  }

  private fun mockGroupData(
    groupRemoteId: Int,
    locationRemoteId: Int,
    locationCaption: String = "",
    locationCollapsed: Boolean = false
  ): ChannelGroupDataEntity {

    val location: LocationEntity = mockk {
      every { remoteId } returns locationRemoteId
      every { caption } returns locationCaption
      every { isCollapsed(CollapsedFlag.GROUP) } returns locationCollapsed
    }

    return mockk {
      every { locationEntity } returns location
      every { getLegacyGroup() } returns mockk()
      every { remoteId } returns groupRemoteId
      every { locationId } returns locationRemoteId
    }
  }
}
