package org.supla.android.data.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.database.Cursor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.ChannelValue;
import org.supla.android.db.Location;
import org.supla.android.db.SuplaContract;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaLocation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@RunWith(MockitoJUnitRunner.class)
public class DefaultChannelRepositoryTest {

    @Mock
    private ChannelDao channelDao;
    @Mock
    private LocationDao locationDao;

    @InjectMocks
    private DefaultChannelRepository defaultChannelRepository;

    @Test
    public void shouldProvideChannelFromDao() {
        // given
        int channelId = 123;
        Channel channel = mock(Channel.class);
        when(channelDao.getChannel(channelId)).thenReturn(channel);

        // when
        Channel result = defaultChannelRepository.getChannel(channelId);

        // then
        Assert.assertSame(channel, result);
        verify(channelDao).getChannel(channelId);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldProvideChannelValueFromDao() {
        // given
        int channelValueId = 123;
        ChannelValue channelValue = mock(ChannelValue.class);
        when(channelDao.getChannelValue(channelValueId)).thenReturn(channelValue);

        // when
        ChannelValue result = defaultChannelRepository.getChannelValue(channelValueId);

        // then
        Assert.assertSame(channelValue, result);
        verify(channelDao).getChannelValue(channelValueId);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldProvideChannelGroupFromDao() {
        // given
        int channelGroupId = 123;
        ChannelGroup channelValue = mock(ChannelGroup.class);
        when(channelDao.getChannelGroup(channelGroupId)).thenReturn(channelValue);

        // when
        ChannelGroup result = defaultChannelRepository.getChannelGroup(channelGroupId);

        // then
        Assert.assertSame(channelValue, result);
        verify(channelDao).getChannelGroup(channelGroupId);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldNotUpdateChannelWhenLocationNotFound() {
        // given
        SuplaChannel suplaChannel = mock(SuplaChannel.class);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertFalse(result);
        verify(locationDao).getLocation(anyInt());
        verifyNoMoreInteractions(locationDao);
        verifyNoInteractions(channelDao);
    }

    @Test
    public void shouldInsertChannelWhenChannelNotFound() {
        // given
        int locationId = 123;
        int channelId = 234;

        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);
        SuplaChannel suplaChannel = suplaChannel(channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7, (short) 8, (short) 9, suplaChannelValue);

        Location location = mock(Location.class);
        when(location.getSorting()).thenReturn(Location.SortingType.DEFAULT);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertTrue(result);
        ArgumentCaptor<Channel> channelArgumentCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).insert(channelArgumentCaptor.capture());
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertChannel(channelArgumentCaptor.getValue(), channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7,
                (short) 8, (short) 9, 1, 0, 12);
    }

    @Test
    public void shouldInsertChannelOnLastPositionWhenUserSortingDefined() {
        // given
        int locationId = 123;
        int channelId = 234;
        int channelsCount = 12;

        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);
        SuplaChannel suplaChannel = suplaChannel(channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7, (short) 8, (short) 9, suplaChannelValue);

        Location location = mock(Location.class);
        when(location.getSorting()).thenReturn(Location.SortingType.USER_DEFINED);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        when(channelDao.getChannelCountForLocation(locationId)).thenReturn(channelsCount);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertTrue(result);
        ArgumentCaptor<Channel> channelArgumentCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).getChannelCountForLocation(locationId);
        verify(channelDao).insert(channelArgumentCaptor.capture());
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertChannel(channelArgumentCaptor.getValue(), channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7,
                (short) 8, (short) 9, 1, channelsCount + 1, 12);
    }

    @Test
    public void shouldUpdateChannelWhenFound() {
        // given
        int locationId = 123;
        int channelId = 234;
        int channelDbId = 345;

        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);
        SuplaChannel suplaChannel = suplaChannel(channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7, (short) 8, (short) 9, suplaChannelValue);

        Location location = mock(Location.class);
        when(location.getSorting()).thenReturn(Location.SortingType.DEFAULT);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        Channel channel = new Channel();
        channel.setId(channelDbId);
        when(channelDao.getChannel(channelId)).thenReturn(channel);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertTrue(result);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).update(channel);
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelDbId, channel.getId());
        assertChannel(channel, channelId, locationId, "caption", 1,
                2, 3, 4, 5, 6, 7,
                (short) 8, (short) 9, 1, 0, 12);
    }

    @Test
    public void shouldUpdateChannelAndChangePositionWhenMovedToAnotherLocation() {
        // given
        int locationId = 123;
        int channelId = 234;
        int channelDbId = 345;
        int channelsCount = 12;

        SuplaChannelValue suplaChannelValue = suplaChannelValue(14L);
        SuplaChannel suplaChannel = suplaChannel(channelId, locationId, "caption1", 9,
                8, 7, 6, 15, 4, 3, (short) 2, (short) 1, suplaChannelValue);

        Location location = mock(Location.class);
        when(location.getSorting()).thenReturn(Location.SortingType.USER_DEFINED);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        Channel channel = new Channel();
        channel.setId(channelDbId);
        channel.setLocationId(locationId + 1);
        when(channelDao.getChannel(channelId)).thenReturn(channel);

        when(channelDao.getChannelCountForLocation(locationId)).thenReturn(channelsCount);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertTrue(result);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).getChannelCountForLocation(locationId);
        verify(channelDao).update(channel);
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelDbId, channel.getId());
        assertChannel(channel, channelId, locationId, "caption1", 9,
                8, 7, 6, 15, 4, 3,
                (short) 2, (short) 1, 1, channelsCount + 1, 14);
    }

    @Test
    public void shouldNotChangeChannelWhenNothingChanged() {
        // given
        int locationId = 123;
        int channelId = 234;
        int channelDbId = 345;

        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);
        SuplaChannel suplaChannel = suplaChannel(channelId, locationId, "caption", 9,
                8, 7, 6, 5, 4, 3, (short) 2, (short) 1, suplaChannelValue);

        Location location = mock(Location.class);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        Channel channel = channel(channelId, locationId, "caption", 9,
                8, 7, 6, 5, 4, 3, (short) 2, (short) 1, channelValue(suplaChannelValue));
        channel.setId(channelDbId);
        channel.setVisible(1);
        when(channelDao.getChannel(channelId)).thenReturn(channel);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        assertFalse(result);
        verify(channelDao).getChannel(channelId);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelDbId, channel.getId());
    }

    @Test
    public void shouldReorderChannels() {
        // given
        int locationId = 2;

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getLong(anyInt())).thenReturn(15L, 12L, 18L, 13L, 14L);
        when(cursor.moveToNext()).thenReturn(true, true, true, true, false);
        when(channelDao.getSortedChannelIdsForLocationCursor(locationId)).thenReturn(cursor);

        // when
        defaultChannelRepository.reorderChannels(15L, locationId, 13L).blockingAwait();

        // then
        ArgumentCaptor<List<Long>> orderArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(channelDao).updateChannelsOrder(orderArgumentCaptor.capture(), eq(locationId));

        List<Long> newOrder = orderArgumentCaptor.getValue();
        assertEquals(12L, (long) newOrder.get(0));
        assertEquals(18L, (long) newOrder.get(1));
        assertEquals(13L, (long) newOrder.get(2));
        assertEquals(15L, (long) newOrder.get(3));
        assertEquals(14L, (long) newOrder.get(4));
    }

    @Test
    public void shouldNotUpdateChannelGroupWhenLocationNotFound() {
        // given
        SuplaChannelGroup suplaChannelGroup = mock(SuplaChannelGroup.class);

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertFalse(result);
        verify(locationDao).getLocation(anyInt());
        verifyNoMoreInteractions(locationDao);
        verifyNoInteractions(channelDao);
    }

    @Test
    public void shouldInsertChannelGroupWhenChannelGroupNotFound() {
        // given
        int locationId = 123;
        int channelGroupId = 234;

        SuplaChannelGroup suplaChannelGroup = suplaChannelGroup(channelGroupId, locationId, "caption", 1,
                2, 3, 4);

        Location location = mock(Location.class);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        when(channelDao.getChannelGroupLastPositionInLocation(locationId)).thenThrow(new NoSuchElementException());

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelGroup> channelArgumentCaptor = ArgumentCaptor.forClass(ChannelGroup.class);
        verify(channelDao).getChannelGroup(channelGroupId);
        verify(channelDao, times(2)).getCachedProfileId();
        verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
        verify(channelDao).insert(channelArgumentCaptor.capture());
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertChannelGroup(channelArgumentCaptor.getValue(), channelGroupId, locationId, "caption", 1,
                2, 3, 4, 1, 0);
    }

    @Test
    public void shouldInsertChannelGroupOnLastPositionWhenPositionsDefined() {
        // given
        int locationId = 123;
        int channelGroupId = 234;
        int channelGroupsCount = 12;

        SuplaChannelGroup suplaChannelGroup = suplaChannelGroup(channelGroupId, locationId, "caption", 1,
                2, 3, 4);


        Location location = mock(Location.class);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        when(channelDao.getChannelGroupLastPositionInLocation(locationId)).thenReturn(channelGroupsCount);

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelGroup> channelGroupArgumentCaptor = ArgumentCaptor.forClass(ChannelGroup.class);
        verify(channelDao).getChannelGroup(channelGroupId);
        verify(channelDao, times(2)).getCachedProfileId();
        verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
        verify(channelDao).insert(channelGroupArgumentCaptor.capture());
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertChannelGroup(channelGroupArgumentCaptor.getValue(), channelGroupId, locationId, "caption", 1,
                2, 3, 4, 1, channelGroupsCount + 1);
    }

    @Test
    public void shouldUpdateChannelGroupWhenFound() {
        // given
        int locationId = 123;
        int channelGroupId = 234;
        int channelGroupDbId = 345;

        SuplaChannelGroup suplaChannelGroup = suplaChannelGroup(channelGroupId, locationId, "caption", 1,
                2, 3, 4);

        Location location = mock(Location.class);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        ChannelGroup channelGroup = new ChannelGroup();
        channelGroup.setId(channelGroupDbId);
        when(channelDao.getChannelGroup(channelGroupId)).thenReturn(channelGroup);

        when(channelDao.getChannelGroupLastPositionInLocation(locationId)).thenThrow(new NoSuchElementException());

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertTrue(result);
        verify(channelDao).getChannelGroup(channelGroupId);
        verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
        verify(channelDao).update(channelGroup);
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelGroupDbId, channelGroup.getId());
        assertChannelGroup(channelGroup, channelGroupId, locationId, "caption", 1,
                2, 3, 4, 1, 0);
    }

    @Test
    public void shouldUpdateChannelGroupAndChangePositionWhenMovedToAnotherLocation() {
        // given
        int locationId = 123;
        int channelGroupId = 234;
        int channelGroupDbId = 345;

        SuplaChannelGroup suplaChannelGroup = suplaChannelGroup(channelGroupId, locationId, "caption1", 9,
                8, 7, 6);

        Location location = mock(Location.class);
        when(location.getLocationId()).thenReturn(locationId);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        ChannelGroup channelGroup = channelGroup(channelGroupId, locationId + 1, "caption1", 9, 8, 7, 6);
        channelGroup.setId(channelGroupDbId);
        channelGroup.setPosition(12);
        when(channelDao.getChannelGroup(channelGroupId)).thenReturn(channelGroup);

        when(channelDao.getChannelGroupLastPositionInLocation(locationId)).thenReturn(0);

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertTrue(result);
        verify(channelDao).getChannelGroup(channelGroupId);
        verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
        verify(channelDao).update(channelGroup);
        verify(channelDao).getCachedProfileId();
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelGroupDbId, channelGroup.getId());
        assertChannelGroup(channelGroup, channelGroupId, locationId, "caption1", 9,
                8, 7, 6, 1, 0);
    }

    @Test
    public void shouldNotChangeChannelGroupWhenNothingChanged() {
        // given
        int locationId = 123;
        int channelGroupId = 234;
        int channelGroupDbId = 345;

        SuplaChannelGroup suplaChannelGroup = suplaChannelGroup(channelGroupId, locationId, "caption", 9,
                8, 7, 6);

        Location location = mock(Location.class);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        ChannelGroup channelGroup = channelGroup(channelGroupId, locationId, "caption", 9,
                8, 7, 6);
        channelGroup.setId(channelGroupDbId);
        channelGroup.setVisible(1);
        when(channelDao.getChannelGroup(channelGroupId)).thenReturn(channelGroup);

        // when
        boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

        // then
        assertFalse(result);
        verify(channelDao).getChannelGroup(channelGroupId);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        assertEquals(channelGroupDbId, channelGroup.getId());
    }

    @Test
    public void shouldInsertChannelValueWhenChannelValueNotFound() {
        // given
        int channelId = 234;
        final boolean online = true;
        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);

        // when
        boolean result = defaultChannelRepository.updateChannelValue(suplaChannelValue, channelId, online);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelValue> channelValueArgumentCaptor = ArgumentCaptor.forClass(ChannelValue.class);
        verify(channelDao).getChannelValue(channelId);
        verify(channelDao).insert(channelValueArgumentCaptor.capture());
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);

        assertChannelValue(channelValueArgumentCaptor.getValue(), 12, channelId, online);
    }

    @Test
    public void shouldUpdateChannelValueWhenChannelValueFoundButDiffers() {
        // given
        int channelId = 234;
        final boolean online = true;
        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);

        ChannelValue channelValue = channelValue(suplaChannelValue(13L));
        channelValue.setChannelId(channelId);
        channelValue.setOnLine(true);
        when(channelDao.getChannelValue(channelId)).thenReturn(channelValue);

        // when
        boolean result = defaultChannelRepository.updateChannelValue(suplaChannelValue, channelId, online);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelValue> channelValueArgumentCaptor = ArgumentCaptor.forClass(ChannelValue.class);
        verify(channelDao).getChannelValue(channelId);
        verify(channelDao).update(channelValueArgumentCaptor.capture());
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);

        assertChannelValue(channelValueArgumentCaptor.getValue(), 12, channelId, online);
    }

    @Test
    public void shouldUpdateChannelValueWhenChannelValueFoundButOnlineFieldChanged() {
        // given
        int channelId = 234;
        final boolean online = true;
        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);

        ChannelValue channelValue = channelValue(suplaChannelValue);
        channelValue.setChannelId(channelId);
        channelValue.setOnLine(false);
        when(channelDao.getChannelValue(channelId)).thenReturn(channelValue);

        // when
        boolean result = defaultChannelRepository.updateChannelValue(suplaChannelValue, channelId, online);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelValue> channelValueArgumentCaptor = ArgumentCaptor.forClass(ChannelValue.class);
        verify(channelDao).getChannelValue(channelId);
        verify(channelDao).update(channelValueArgumentCaptor.capture());
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);

        assertChannelValue(channelValueArgumentCaptor.getValue(), 12, channelId, online);
    }

    @Test
    public void shouldNotUpdateChannelValueWhenNoChanges() {
        // given
        int channelId = 234;
        final boolean online = true;
        SuplaChannelValue suplaChannelValue = suplaChannelValue(12L);

        ChannelValue channelValue = channelValue(suplaChannelValue);
        channelValue.setChannelId(channelId);
        channelValue.setOnLine(online);
        when(channelDao.getChannelValue(channelId)).thenReturn(channelValue);

        // when
        boolean result = defaultChannelRepository.updateChannelValue(suplaChannelValue, channelId, online);

        // then
        assertFalse(result);
        verify(channelDao).getChannelValue(channelId);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldInsertChannelExtendedValueWhenChannelExtendedValueNotFound() {
        // given
        int channelId = 234;
        SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);

        // when
        boolean result = defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelExtendedValue> argumentCaptor = ArgumentCaptor.forClass(ChannelExtendedValue.class);
        verify(channelDao).getChannelExtendedValue(channelId);
        verify(channelDao).insert(argumentCaptor.capture());
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);

        assertSame(suplaChannelExtendedValue, argumentCaptor.getValue().getExtendedValue());
        assertEquals(channelId, argumentCaptor.getValue().getChannelId());
    }

    @Test
    public void shouldUpdateChannelExtendedValueWhenChannelExtendedValueFound() {
        // given
        int channelId = 234;
        SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);

        ChannelExtendedValue channelExtendedValue = mock(ChannelExtendedValue.class);
        when(channelDao.getChannelExtendedValue(channelId)).thenReturn(channelExtendedValue);

        // when
        boolean result = defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

        // then
        assertTrue(result);
        verify(channelDao).getChannelExtendedValue(channelId);
        verify(channelDao).update(channelExtendedValue);
        verify(channelExtendedValue).setExtendedValue(suplaChannelExtendedValue);
        verifyNoMoreInteractions(channelDao, channelExtendedValue);
        verifyNoInteractions(locationDao);

    }

    @Test
    public void shouldInsertChannelGroupRelationWhenChannelGroupRelationNotFound() {
        // given
        int channelId = 234;
        int groupId = 123;
        SuplaChannelGroupRelation suplaChannelGroupRelation = new SuplaChannelGroupRelation();
        suplaChannelGroupRelation.ChannelID = channelId;
        suplaChannelGroupRelation.ChannelGroupID = groupId;

        // when
        boolean result = defaultChannelRepository.updateChannelGroupRelation(suplaChannelGroupRelation);

        // then
        assertTrue(result);
        ArgumentCaptor<ChannelGroupRelation> argumentCaptor = ArgumentCaptor.forClass(ChannelGroupRelation.class);
        verify(channelDao).getChannelGroupRelation(channelId, groupId);
        verify(channelDao).insert(argumentCaptor.capture());
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);

        assertEquals(channelId, argumentCaptor.getValue().getChannelId());
        assertEquals(groupId, argumentCaptor.getValue().getGroupId());
        assertEquals(1, argumentCaptor.getValue().getVisible());
    }

    @Test
    public void shouldUpdateChannelGroupRelationWhenChannelGroupRelationFound() {
        // given
        int channelId = 234;
        int groupId = 123;
        SuplaChannelGroupRelation suplaChannelGroupRelation = new SuplaChannelGroupRelation();
        suplaChannelGroupRelation.ChannelID = channelId;
        suplaChannelGroupRelation.ChannelGroupID = groupId;

        ChannelGroupRelation channelGroupRelation = mock(ChannelGroupRelation.class);
        when(channelDao.getChannelGroupRelation(channelId, groupId)).thenReturn(channelGroupRelation);

        // when
        boolean result = defaultChannelRepository.updateChannelGroupRelation(suplaChannelGroupRelation);

        // then
        assertTrue(result);
        verify(channelDao).getChannelGroupRelation(channelId, groupId);
        verify(channelDao).update(channelGroupRelation);
        verify(channelGroupRelation).getVisible();
        verify(channelGroupRelation).Assign(suplaChannelGroupRelation);
        verify(channelGroupRelation).setVisible(1);
        verifyNoMoreInteractions(channelDao, channelGroupRelation);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldNotUpdateChannelGroupRelationWhenFoundAndVisible() {
        // given
        int channelId = 234;
        int groupId = 123;
        SuplaChannelGroupRelation suplaChannelGroupRelation = new SuplaChannelGroupRelation();
        suplaChannelGroupRelation.ChannelID = channelId;
        suplaChannelGroupRelation.ChannelGroupID = groupId;

        ChannelGroupRelation channelGroupRelation = mock(ChannelGroupRelation.class);
        when(channelGroupRelation.getVisible()).thenReturn(1);
        when(channelDao.getChannelGroupRelation(channelId, groupId)).thenReturn(channelGroupRelation);

        // when
        boolean result = defaultChannelRepository.updateChannelGroupRelation(suplaChannelGroupRelation);

        // then
        assertFalse(result);
        verify(channelDao).getChannelGroupRelation(channelId, groupId);
        verify(channelGroupRelation).getVisible();
        verifyNoMoreInteractions(channelDao, channelGroupRelation);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldProvideChannelCountFromDao() {
        // given
        int channelCount = 123;
        when(channelDao.getChannelCount()).thenReturn(channelCount);

        // when
        int result = defaultChannelRepository.getChannelCount();

        // then
        assertEquals(channelCount, result);
        verify(channelDao).getChannelCount();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldSetChannelsVisible() {
        // given
        int visible = 1;
        int whereVisible = 2;
        final boolean expectedResult = true;
        when(channelDao.setChannelsVisible(visible, whereVisible)).thenReturn(expectedResult);

        // when
        boolean result = defaultChannelRepository.setChannelsVisible(visible, whereVisible);

        // then
        assertEquals(expectedResult, result);
        verify(channelDao).setChannelsVisible(visible, whereVisible);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldSetChannelGroupsVisible() {
        // given
        int visible = 1;
        int whereVisible = 2;
        final boolean expectedResult = true;
        when(channelDao.setChannelGroupsVisible(visible, whereVisible)).thenReturn(expectedResult);

        // when
        boolean result = defaultChannelRepository.setChannelGroupsVisible(visible, whereVisible);

        // then
        assertEquals(expectedResult, result);
        verify(channelDao).setChannelGroupsVisible(visible, whereVisible);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldSetChannelGroupRelationsVisible() {
        // given
        int visible = 1;
        int whereVisible = 2;
        final boolean expectedResult = true;
        when(channelDao.setChannelGroupRelationsVisible(visible, whereVisible)).thenReturn(expectedResult);

        // when
        boolean result = defaultChannelRepository.setChannelGroupRelationsVisible(visible, whereVisible);

        // then
        assertEquals(expectedResult, result);
        verify(channelDao).setChannelGroupRelationsVisible(visible, whereVisible);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldSetChannelOffline() {
        // given
        final boolean expectedResult = true;
        when(channelDao.setChannelsOffline()).thenReturn(expectedResult);

        // when
        boolean result = defaultChannelRepository.setChannelsOffline();

        // then
        assertEquals(expectedResult, result);
        verify(channelDao).setChannelsOffline();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetChannelListCursorWithDefaultOrder() {
        // given
        String where = SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " <> 0 ";
        Cursor expectedResult = mock(Cursor.class);
        when(channelDao.getChannelListCursorWithDefaultOrder(where)).thenReturn(expectedResult);

        // when
        Cursor result = defaultChannelRepository.getChannelListCursorWithDefaultOrder();

        // then
        assertSame(expectedResult, result);
        verify(channelDao).getChannelListCursorWithDefaultOrder(where);
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetChannelGroupListCursor() {
        // given
        Cursor expectedResult = mock(Cursor.class);
        when(channelDao.getChannelGroupListCursor()).thenReturn(expectedResult);

        // when
        Cursor result = defaultChannelRepository.getChannelGroupListCursor();

        // then
        assertSame(expectedResult, result);
        verify(channelDao).getChannelGroupListCursor();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetIfZWaveBridgeChannelIsAvailable() {
        // given
        final boolean expectedResult = true;
        when(channelDao.isZWaveBridgeChannelAvailable()).thenReturn(expectedResult);

        // when
        boolean result = defaultChannelRepository.isZWaveBridgeChannelAvailable();

        // then
        assertSame(expectedResult, result);
        verify(channelDao).isZWaveBridgeChannelAvailable();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetZWaveBridgeChannels() {
        // given
        List<Channel> expectedResult = new ArrayList<>();
        when(channelDao.getZWaveBridgeChannels()).thenReturn(expectedResult);

        // when
        List<Channel> result = defaultChannelRepository.getZWaveBridgeChannels();

        // then
        assertSame(expectedResult, result);
        verify(channelDao).getZWaveBridgeChannels();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetChannelUserIconIds() {
        // given
        when(channelDao.getChannelUserIconIds()).thenReturn(Arrays.asList(1, 2));
        when(channelDao.getChannelGroupUserIconIds()).thenReturn(Arrays.asList(3, 4));

        // when
        List<Integer> result = defaultChannelRepository.getChannelUserIconIds();

        // then
        assertEquals(4, result.size());
        assertEquals(1, (int) result.get(0));
        assertEquals(2, (int) result.get(1));
        assertEquals(3, (int) result.get(2));
        assertEquals(4, (int) result.get(3));

        verify(channelDao).getChannelUserIconIds();
        verify(channelDao).getChannelGroupUserIconIds();
        verifyNoMoreInteractions(channelDao);
        verifyNoInteractions(locationDao);
    }

    @Test
    public void shouldGetLocation() {
        // given
        int locationId = 1;
        Location expectedResult = mock(Location.class);
        when(locationDao.getLocation(locationId)).thenReturn(expectedResult);

        // when
        Location result = defaultChannelRepository.getLocation(locationId);

        // then
        assertSame(expectedResult, result);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao);
        verifyNoInteractions(channelDao);
    }

    @Test
    public void shouldInsertSuplaLocationWhenLocationNotFound() {
        // given
        int locationId = 234;
        String caption = "caption";
        SuplaLocation suplaLocation = suplaLocation(locationId, caption);

        // when
        boolean result = defaultChannelRepository.updateLocation(suplaLocation);

        // then
        assertTrue(result);
        ArgumentCaptor<Location> argumentCaptor = ArgumentCaptor.forClass(Location.class);
        verify(locationDao).getLocation(locationId);
        verify(locationDao).insert(argumentCaptor.capture());
        verifyNoMoreInteractions(locationDao);
        verifyNoInteractions(channelDao);

        assertEquals(locationId, argumentCaptor.getValue().getLocationId());
        assertEquals(caption, argumentCaptor.getValue().getCaption());
        assertEquals(1, argumentCaptor.getValue().getVisible());
        assertEquals(Location.SortingType.DEFAULT, argumentCaptor.getValue().getSorting());
    }

    @Test
    public void shouldUpdateSuplaLocationWhenLocationFound() {
        // given
        int locationId = 234;
        String caption = "caption";
        SuplaLocation suplaLocation = suplaLocation(locationId, caption);

        Location location = mock(Location.class);
        when(location.Diff(suplaLocation)).thenReturn(true);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        // when
        boolean result = defaultChannelRepository.updateLocation(suplaLocation);

        // then
        assertTrue(result);
        verify(locationDao).getLocation(locationId);
        verify(locationDao).update(location);
        verify(location).Diff(suplaLocation);
        verify(location).AssignSuplaLocation(suplaLocation);
        verify(location).setVisible(1);
        verifyNoMoreInteractions(locationDao, location);
        verifyNoInteractions(channelDao);
    }

    @Test
    public void shouldNotUpdateSuplaLocationWhenFoundAndDoNotDiffer() {
        // given
        int locationId = 234;
        String caption = "caption";
        SuplaLocation suplaLocation = suplaLocation(locationId, caption);

        Location location = mock(Location.class);
        when(location.Diff(suplaLocation)).thenReturn(false);
        when(locationDao.getLocation(locationId)).thenReturn(location);

        // when
        boolean result = defaultChannelRepository.updateLocation(suplaLocation);

        // then
        assertFalse(result);
        verify(locationDao).getLocation(locationId);
        verify(location).Diff(suplaLocation);
        verifyNoMoreInteractions(locationDao, location);
        verifyNoInteractions(channelDao);
    }

    @Test
    public void shouldUpdateLocationWhenNotNull() {
        // given
        Location location = mock(Location.class);

        // when
        defaultChannelRepository.updateLocation(location);

        // then
        verify(locationDao).update(location);
        verifyNoMoreInteractions(locationDao);
        verifyNoInteractions(channelDao, location);
    }

    @Test
    public void shouldUpdateLocationWhenNull() {
        // given
        Location location = null;

        // when
        defaultChannelRepository.updateLocation(location);

        // then
        verifyNoInteractions(locationDao, channelDao);
    }

    @Test
    public void shouldReorderChannelGroups() {
        // given
        int locationId = 2;

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getLong(anyInt())).thenReturn(15L, 12L, 18L, 13L, 14L);
        when(cursor.moveToNext()).thenReturn(true, true, true, true, false);
        when(channelDao.getSortedChannelGroupIdsForLocationCursor(locationId)).thenReturn(cursor);

        // when
        defaultChannelRepository.reorderChannelGroups(15L, locationId, 13L).blockingAwait();

        // then
        ArgumentCaptor<List<Long>> orderArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(channelDao).updateChannelGroupsOrder(orderArgumentCaptor.capture());

        List<Long> newOrder = orderArgumentCaptor.getValue();
        assertEquals(12L, (long) newOrder.get(0));
        assertEquals(18L, (long) newOrder.get(1));
        assertEquals(13L, (long) newOrder.get(2));
        assertEquals(15L, (long) newOrder.get(3));
        assertEquals(14L, (long) newOrder.get(4));
    }

    @Test
    public void shouldGetAllChannelsForProfile() {
        // given
        long profileId = 123L;
        Cursor cursor = mock(Cursor.class);
        when(channelDao.getAllChannels("func <> 0  AND (C.profileid = " + profileId + ") ")).thenReturn(cursor);

        // when
        Cursor returned = defaultChannelRepository.getAllProfileChannels(profileId);

        // then
        assertSame(cursor, returned);
    }

    private void assertChannel(Channel channel, int id, int locationId, String caption, int func, int flags,
                               int altIcon, int userIcon, int deviceId, int type, int protocolVersion,
                               short manufacturerId, short productId, int visible, int position, int valueAsLong) {
        assertEquals(id, channel.getChannelId());
        assertEquals(locationId, channel.getLocationId());
        assertEquals(caption, channel.getCaption());
        assertEquals(func, channel.getFunc());
        assertEquals(flags, channel.getFlags());
        assertEquals(altIcon, channel.getAltIcon());
        assertEquals(userIcon, channel.getUserIconId());
        assertEquals(deviceId, channel.getDeviceID());
        assertEquals(type, channel.getType());
        assertEquals(protocolVersion, channel.getProtocolVersion());
        assertEquals(manufacturerId, channel.getManufacturerID());
        assertEquals(productId, channel.getProductID());
        assertEquals(visible, channel.getVisible());
        assertEquals(position, channel.getPosition());

        ChannelValue channelValue = channel.getValue();
        assertEquals(valueAsLong, channelValue.getLong());
    }

    private void assertChannelValue(ChannelValue channelValue, long valueAsLong, int channelId, boolean online) {
        assertEquals(valueAsLong, channelValue.getLong());
        assertEquals(channelId, channelValue.getChannelId());
        assertEquals(online, channelValue.getOnLine());
    }

    private void assertChannelGroup(ChannelGroup channelGroup, int id, int locationId, String caption, int func, int flags,
                                    int altIcon, int userIcon, int visible, int position) {
        assertEquals(id, channelGroup.getGroupId());
        assertEquals(locationId, channelGroup.getLocationId());
        assertEquals(caption, channelGroup.getCaption());
        assertEquals(func, channelGroup.getFunc());
        assertEquals(flags, channelGroup.getFlags());
        assertEquals(altIcon, channelGroup.getAltIcon());
        assertEquals(userIcon, channelGroup.getUserIconId());
        assertEquals(0, channelGroup.getType());
        assertEquals(visible, channelGroup.getVisible());
        assertEquals(position, channelGroup.getPosition());
    }

    private SuplaChannel suplaChannel(int id, int locationId, String caption, int func, int flags,
                                      int altIcon, int userIcon, int deviceId, int type, int protocolVersion,
                                      short manufacturerId, short productId, SuplaChannelValue suplaChannelValue) {
        SuplaChannel suplaChannel = new SuplaChannel();
        suplaChannel.Id = id;
        suplaChannel.LocationID = locationId;
        suplaChannel.Caption = caption;
        suplaChannel.Func = func;
        suplaChannel.Flags = flags;
        suplaChannel.AltIcon = altIcon;
        suplaChannel.UserIcon = userIcon;

        suplaChannel.DeviceID = deviceId;
        suplaChannel.Type = type;
        suplaChannel.ProtocolVersion = protocolVersion;
        suplaChannel.ManufacturerID = manufacturerId;
        suplaChannel.ProductID = productId;
        suplaChannel.Value = suplaChannelValue;

        return suplaChannel;
    }

    private SuplaChannelGroup suplaChannelGroup(int id, int locationId, String caption, int func, int flags,
                                                int altIcon, int userIcon) {
        SuplaChannelGroup suplaChannelGroup = new SuplaChannelGroup();
        suplaChannelGroup.Id = id;
        suplaChannelGroup.LocationID = locationId;
        suplaChannelGroup.Caption = caption;
        suplaChannelGroup.Func = func;
        suplaChannelGroup.Flags = flags;
        suplaChannelGroup.AltIcon = altIcon;
        suplaChannelGroup.UserIcon = userIcon;

        return suplaChannelGroup;
    }

    private SuplaChannelValue suplaChannelValue(Long value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);

        SuplaChannelValue suplaChannelValue = new SuplaChannelValue();
        byteBuffer.putLong(0, value);
        suplaChannelValue.Value = invert(byteBuffer.array());

        return suplaChannelValue;
    }

    private SuplaLocation suplaLocation(int locationId, String caption) {
        SuplaLocation suplaLocation = new SuplaLocation();
        suplaLocation.Id = locationId;
        suplaLocation.Caption = caption;

        return suplaLocation;
    }

    private Channel channel(int id, int locationId, String caption, int func, int flags,
                            int altIcon, int userIcon, int deviceId, int type, int protocolVersion,
                            short manufacturerId, short productId, ChannelValue channelValue) {
        Channel channel = new Channel();
        channel.setRemoteId(id);
        channel.setLocationId(locationId);
        channel.setCaption(caption);
        channel.setFunc(func);
        channel.setFlags(flags);
        channel.setAltIcon(altIcon);
        channel.setUserIconId(userIcon);

        channel.setDeviceID(deviceId);
        channel.setType(type);
        channel.setProtocolVersion(protocolVersion);
        channel.setManufacturerID(manufacturerId);
        channel.setProductID(productId);
        channel.setValue(channelValue);

        return channel;
    }

    private ChannelGroup channelGroup(int id, int locationId, String caption, int func, int flags,
                                      int altIcon, int userIcon) {
        ChannelGroup channelGroup = new ChannelGroup();
        channelGroup.setRemoteId(id);
        channelGroup.setLocationId(locationId);
        channelGroup.setCaption(caption);
        channelGroup.setFunc(func);
        channelGroup.setFlags(flags);
        channelGroup.setAltIcon(altIcon);
        channelGroup.setUserIconId(userIcon);

        return channelGroup;
    }

    private ChannelValue channelValue(SuplaChannelValue suplaChannelValue) {
        ChannelValue channelValue = new ChannelValue();
        channelValue.AssignSuplaChannelValue(suplaChannelValue);
        return channelValue;
    }

    private byte[] invert(byte[] array) {
        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - i - 1];
        }

        return result;
    }
}
