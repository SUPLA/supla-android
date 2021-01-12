package org.supla.android.data.source;

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
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelValue;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelValue;

import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
        verifyZeroInteractions(locationDao);
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
        verifyZeroInteractions(locationDao);
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
        verifyZeroInteractions(locationDao);
    }

    @Test
    public void shouldNotUpdateChannelWhenLocationNotFound() {
        // given
        SuplaChannel suplaChannel = mock(SuplaChannel.class);

        // when
        boolean result = defaultChannelRepository.updateChannel(suplaChannel);

        // then
        Assert.assertFalse(result);
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
        Assert.assertTrue(result);
        ArgumentCaptor<Channel> channelArgumentCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).insert(channelArgumentCaptor.capture());
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
        Assert.assertTrue(result);
        ArgumentCaptor<Channel> channelArgumentCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).getChannelCountForLocation(locationId);
        verify(channelDao).insert(channelArgumentCaptor.capture());
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
        Assert.assertTrue(result);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).update(channel);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        Assert.assertEquals(channelDbId, channel.getId());
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
        Assert.assertTrue(result);
        verify(channelDao).getChannel(channelId);
        verify(channelDao).getChannelCountForLocation(locationId);
        verify(channelDao).update(channel);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        Assert.assertEquals(channelDbId, channel.getId());
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
        Assert.assertFalse(result);
        verify(channelDao).getChannel(channelId);
        verify(locationDao).getLocation(locationId);
        verifyNoMoreInteractions(locationDao, channelDao);

        Assert.assertEquals(channelDbId, channel.getId());
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
        Assert.assertEquals(12L, (long) newOrder.get(0));
        Assert.assertEquals(18L, (long) newOrder.get(1));
        Assert.assertEquals(13L, (long) newOrder.get(2));
        Assert.assertEquals(15L, (long) newOrder.get(3));
        Assert.assertEquals(14L, (long) newOrder.get(4));
    }

    private void assertChannel(Channel channel, int id, int locationId, String caption, int func, int flags,
                               int altIcon, int userIcon, int deviceId, int type, int protocolVersion,
                               short manufacturerId, short productId, int visible, int position, int valueAsLong) {
        Assert.assertEquals(id, channel.getChannelId());
        Assert.assertEquals(locationId, channel.getLocationId());
        Assert.assertEquals(caption, channel.getCaption());
        Assert.assertEquals(func, channel.getFunc());
        Assert.assertEquals(flags, channel.getFlags());
        Assert.assertEquals(altIcon, channel.getAltIcon());
        Assert.assertEquals(userIcon, channel.getUserIconId());
        Assert.assertEquals(deviceId, channel.getDeviceID());
        Assert.assertEquals(type, channel.getType());
        Assert.assertEquals(protocolVersion, channel.getProtocolVersion());
        Assert.assertEquals(manufacturerId, channel.getManufacturerID());
        Assert.assertEquals(productId, channel.getProductID());
        Assert.assertEquals(visible, channel.getVisible());
        Assert.assertEquals(position, channel.getPosition());

        ChannelValue channelValue = channel.getValue();
        Assert.assertEquals(valueAsLong, channelValue.getLong());
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

    private SuplaChannelValue suplaChannelValue(Long value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);

        SuplaChannelValue suplaChannelValue = new SuplaChannelValue();
        byteBuffer.putLong(0, value);
        suplaChannelValue.Value = invert(byteBuffer.array());

        return suplaChannelValue;
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