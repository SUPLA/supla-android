package org.supla.android.data.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.core.infrastructure.DateProvider;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaLocation;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultChannelRepositoryTest {

  @Mock private ChannelDao channelDao;
  @Mock private LocationDao locationDao;
  @Mock private DateProvider dateProvider;

  @InjectMocks private DefaultChannelRepository defaultChannelRepository;

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
  public void shouldReorderChannels() {
    // given
    int locationId = 2;
    String locationCaption = "Location";

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(anyInt())).thenReturn(15L, 12L, 18L, 13L, 14L);
    when(cursor.moveToNext()).thenReturn(true, true, true, true, false);
    when(channelDao.getSortedChannelIdsForLocationCursor(locationCaption)).thenReturn(cursor);

    Location location = mock(Location.class);
    when(location.getCaption()).thenReturn(locationCaption);
    when(locationDao.getLocation(locationId)).thenReturn(location);

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
    when(channelDao.getChannelUserIconIdsToDownload()).thenReturn(Arrays.asList(1, 2));
    when(channelDao.getChannelGroupUserIconIdsToDownload()).thenReturn(Arrays.asList(3, 4));

    // when
    List<Integer> result = defaultChannelRepository.getChannelUserIconIdsToDownload();

    // then
    assertEquals(4, result.size());
    assertEquals(1, (int) result.get(0));
    assertEquals(2, (int) result.get(1));
    assertEquals(3, (int) result.get(2));
    assertEquals(4, (int) result.get(3));

    verify(channelDao).getChannelUserIconIdsToDownload();
    verify(channelDao).getChannelGroupUserIconIdsToDownload();
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
  public void shouldReorderChannelGroups() {
    // given
    int locationId = 2;
    String locationCaption = "Caption";

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(anyInt())).thenReturn(15L, 12L, 18L, 13L, 14L);
    when(cursor.moveToNext()).thenReturn(true, true, true, true, false);
    when(channelDao.getSortedChannelGroupIdsForLocationCursor(locationCaption)).thenReturn(cursor);

    Location location = mock(Location.class);
    when(location.getCaption()).thenReturn(locationCaption);
    when(locationDao.getLocation(locationId)).thenReturn(location);

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

  private void assertChannelGroup(
      ChannelGroup channelGroup,
      int id,
      int locationId,
      String caption,
      int func,
      int flags,
      int altIcon,
      int userIcon,
      int position) {
    assertEquals(id, channelGroup.getGroupId());
    assertEquals(locationId, channelGroup.getLocationId());
    assertEquals(caption, channelGroup.getCaption(null));
    assertEquals(func, channelGroup.getFunc());
    assertEquals(flags, channelGroup.getFlags());
    assertEquals(altIcon, channelGroup.getAltIcon());
    assertEquals(userIcon, channelGroup.getUserIconId());
    assertEquals(0, channelGroup.getType());
    assertEquals(1, channelGroup.getVisible());
    assertEquals(position, channelGroup.getPosition());
  }

  private SuplaChannelGroup suplaChannelGroup(
      int id, int locationId, String caption, int func, int flags, int altIcon, int userIcon) {
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

  private SuplaLocation suplaLocation(int locationId, String caption) {
    SuplaLocation suplaLocation = new SuplaLocation();
    suplaLocation.Id = locationId;
    suplaLocation.Caption = caption;

    return suplaLocation;
  }

  @SuppressWarnings("SameParameterValue")
  private ChannelGroup channelGroup(
      int id, int locationId, String caption, int func, int flags, int altIcon, int userIcon) {
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
}
