package org.supla.android.usecases.channel

import android.database.Cursor
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Location
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag

@RunWith(MockitoJUnitRunner::class)
class CreateProfileGroupsListUseCaseTest {
  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var profileManager: ProfileManager

  @InjectMocks
  private lateinit var usecase: CreateProfileGroupsListUseCase

  @Test
  fun `should create list of channels and locations`() {
    // given
    val profileId = 987L

    val locationColumn = 123
    val idColumn = 234

    val firstLocationId = 1L
    val collapsedLocationId = 2L
    val thirdLocationId = 3L
    val cursor: Cursor = Mockito.mock(Cursor::class.java)
    whenever(cursor.moveToFirst()).thenReturn(true)
    whenever(cursor.moveToNext()).thenReturn(true, true, true, false)
    whenever(cursor.getColumnIndex(ChannelEntity.COLUMN_LOCATION_ID)).thenReturn(locationColumn)
    whenever(cursor.getColumnIndex(ChannelEntity.COLUMN_ID)).thenReturn(idColumn)
    whenever(cursor.getLong(locationColumn)).thenReturn(firstLocationId, firstLocationId, collapsedLocationId, thirdLocationId)
    whenever(cursor.getLong(idColumn)).thenReturn(11L, 22L, 33L, 44L)

    val firstLocation = Mockito.mock(Location::class.java)
    whenever(firstLocation.locationId).thenReturn(firstLocationId.toInt())
    whenever(firstLocation.caption).thenReturn("Location")
    whenever(channelRepository.getLocation(firstLocationId.toInt())).thenReturn(firstLocation)

    val collapsedLocation = Mockito.mock(Location::class.java)
    whenever(collapsedLocation.locationId).thenReturn(collapsedLocationId.toInt())
    whenever(collapsedLocation.caption).thenReturn("Collapsed location")
    whenever(collapsedLocation.collapsed).thenReturn(0 or CollapsedFlag.GROUP.value)
    whenever(channelRepository.getLocation(collapsedLocationId.toInt())).thenReturn(collapsedLocation)

    val thirdLocation = Mockito.mock(Location::class.java)
    whenever(channelRepository.getLocation(thirdLocationId.toInt())).thenReturn(thirdLocation)

    val profile = Mockito.mock(AuthProfileItem::class.java)
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))

    whenever(channelRepository.getAllProfileChannelGroups(profileId)).thenReturn(cursor)

    // when
    val testObserver = usecase().test()

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

    Assertions.assertThat((list[1] as ListItem.ChannelItem).channelBase.id).isEqualTo(11)
    Assertions.assertThat((list[2] as ListItem.ChannelItem).channelBase.id).isEqualTo(22)
    Assertions.assertThat((list[5] as ListItem.ChannelItem).channelBase.id).isEqualTo(44)

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[3] as ListItem.LocationItem).location).isEqualTo(collapsedLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val profileId = 987L

    val locationColumn = 123
    val idColumn = 234

    val firstLocationId = 1L
    val secondLocationId = 2L
    val thirdLocationId = 3L
    val cursor: Cursor = Mockito.mock(Cursor::class.java)
    whenever(cursor.moveToFirst()).thenReturn(true)
    whenever(cursor.moveToNext()).thenReturn(true, true, true, false)
    whenever(cursor.getColumnIndex(ChannelEntity.COLUMN_LOCATION_ID)).thenReturn(locationColumn)
    whenever(cursor.getColumnIndex(ChannelEntity.COLUMN_ID)).thenReturn(idColumn)
    whenever(cursor.getLong(locationColumn)).thenReturn(firstLocationId, firstLocationId, secondLocationId, thirdLocationId)
    whenever(cursor.getLong(idColumn)).thenReturn(11L, 22L, 33L, 44L)

    val firstLocation = Mockito.mock(Location::class.java)
    whenever(firstLocation.locationId).thenReturn(firstLocationId.toInt())
    whenever(firstLocation.caption).thenReturn("Test")
    whenever(channelRepository.getLocation(firstLocationId.toInt())).thenReturn(firstLocation)

    val secondLocation = Mockito.mock(Location::class.java)
    whenever(secondLocation.caption).thenReturn("Test")
    whenever(channelRepository.getLocation(secondLocationId.toInt())).thenReturn(secondLocation)

    val thirdLocation = Mockito.mock(Location::class.java)
    whenever(channelRepository.getLocation(thirdLocationId.toInt())).thenReturn(thirdLocation)

    val profile = Mockito.mock(AuthProfileItem::class.java)
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))

    whenever(channelRepository.getAllProfileChannelGroups(profileId)).thenReturn(cursor)

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

    Assertions.assertThat((list[1] as ListItem.ChannelItem).channelBase.id).isEqualTo(11)
    Assertions.assertThat((list[2] as ListItem.ChannelItem).channelBase.id).isEqualTo(22)
    Assertions.assertThat((list[3] as ListItem.ChannelItem).channelBase.id).isEqualTo(33)
    Assertions.assertThat((list[5] as ListItem.ChannelItem).channelBase.id).isEqualTo(44)

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }
}
