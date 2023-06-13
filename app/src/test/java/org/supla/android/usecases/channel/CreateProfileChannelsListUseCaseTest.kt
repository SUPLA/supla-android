package org.supla.android.usecases.channel

import android.database.Cursor
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Location
import org.supla.android.db.SuplaContract
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag

@RunWith(MockitoJUnitRunner::class)
class CreateProfileChannelsListUseCaseTest {
  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var profileManager: ProfileManager

  @InjectMocks
  private lateinit var usecase: CreateProfileChannelsListUseCase

  @Test
  fun `should create list of channels and locations`() {
    // given
    val profileId = 987L

    val locationColumn = 123
    val idColumn = 234

    val firstLocationId = 1L
    val collapsedLocationId = 2L
    val thirdLocationId = 3L
    val cursor: Cursor = mock(Cursor::class.java)
    whenever(cursor.moveToFirst()).thenReturn(true)
    whenever(cursor.moveToNext()).thenReturn(true, true, true, false)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID)).thenReturn(locationColumn)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelEntry._ID)).thenReturn(idColumn)
    whenever(cursor.getLong(locationColumn)).thenReturn(firstLocationId, firstLocationId, collapsedLocationId, thirdLocationId)
    whenever(cursor.getLong(idColumn)).thenReturn(11L, 22L, 33L, 44L)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE)).thenReturn(-1)

    val firstLocation = mock(Location::class.java)
    whenever(firstLocation.locationId).thenReturn(firstLocationId.toInt())
    whenever(firstLocation.caption).thenReturn("Caption 1")
    whenever(channelRepository.getLocation(firstLocationId.toInt())).thenReturn(firstLocation)

    val collapsedLocation = mock(Location::class.java)
    whenever(collapsedLocation.locationId).thenReturn(collapsedLocationId.toInt())
    whenever(collapsedLocation.caption).thenReturn("collapsed location")
    whenever(collapsedLocation.collapsed).thenReturn(0 or CollapsedFlag.CHANNEL.value)
    whenever(channelRepository.getLocation(collapsedLocationId.toInt())).thenReturn(collapsedLocation)

    val thirdLocation = mock(Location::class.java)
    whenever(channelRepository.getLocation(thirdLocationId.toInt())).thenReturn(thirdLocation)

    val profile = mock(AuthProfileItem::class.java)
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))

    whenever(channelRepository.getAllProfileChannels(profileId)).thenReturn(cursor)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.ChannelItem::class.java)

    assertThat((list[1] as ListItem.ChannelItem).channelBase.id).isEqualTo(11)
    assertThat((list[2] as ListItem.ChannelItem).channelBase.id).isEqualTo(22)
    assertThat((list[5] as ListItem.ChannelItem).channelBase.id).isEqualTo(44)

    assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    assertThat((list[3] as ListItem.LocationItem).location).isEqualTo(collapsedLocation)
    assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
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
    val cursor: Cursor = mock(Cursor::class.java)
    whenever(cursor.moveToFirst()).thenReturn(true)
    whenever(cursor.moveToNext()).thenReturn(true, true, true, false)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID)).thenReturn(locationColumn)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelEntry._ID)).thenReturn(idColumn)
    whenever(cursor.getLong(locationColumn)).thenReturn(firstLocationId, firstLocationId, secondLocationId, thirdLocationId)
    whenever(cursor.getLong(idColumn)).thenReturn(11L, 22L, 33L, 44L)
    whenever(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE)).thenReturn(-1)

    val firstLocation = mock(Location::class.java)
    whenever(firstLocation.locationId).thenReturn(firstLocationId.toInt())
    whenever(firstLocation.caption).thenReturn("Test")
    whenever(channelRepository.getLocation(firstLocationId.toInt())).thenReturn(firstLocation)

    val secondLocation = mock(Location::class.java)
    whenever(secondLocation.caption).thenReturn("Test")
    whenever(channelRepository.getLocation(secondLocationId.toInt())).thenReturn(secondLocation)

    val thirdLocation = mock(Location::class.java)
    whenever(channelRepository.getLocation(thirdLocationId.toInt())).thenReturn(thirdLocation)

    val profile = mock(AuthProfileItem::class.java)
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))

    whenever(channelRepository.getAllProfileChannels(profileId)).thenReturn(cursor)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.ChannelItem::class.java)

    assertThat((list[1] as ListItem.ChannelItem).channelBase.id).isEqualTo(11)
    assertThat((list[2] as ListItem.ChannelItem).channelBase.id).isEqualTo(22)
    assertThat((list[3] as ListItem.ChannelItem).channelBase.id).isEqualTo(33)
    assertThat((list[5] as ListItem.ChannelItem).channelBase.id).isEqualTo(44)

    assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }
}
