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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelGroupRelation;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaLocation;
import org.supla.android.lib.SuplaTimerState;

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

    SuplaChannelGroup suplaChannelGroup =
        suplaChannelGroup(channelGroupId, locationId, "caption", 1, 2, 3, 4);

    Location location = mock(Location.class);
    when(location.getLocationId()).thenReturn(locationId);
    when(locationDao.getLocation(locationId)).thenReturn(location);

    when(channelDao.getChannelGroupLastPositionInLocation(locationId))
        .thenThrow(new NoSuchElementException());

    // when
    boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

    // then
    assertTrue(result);
    ArgumentCaptor<ChannelGroup> channelArgumentCaptor =
        ArgumentCaptor.forClass(ChannelGroup.class);
    verify(channelDao).getChannelGroup(channelGroupId);
    verify(channelDao, times(2)).getCachedProfileId();
    verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
    verify(channelDao).insert(channelArgumentCaptor.capture());
    verify(locationDao).getLocation(locationId);
    verifyNoMoreInteractions(locationDao, channelDao);

    assertChannelGroup(
        channelArgumentCaptor.getValue(), channelGroupId, locationId, "caption", 1, 2, 3, 4, 0);
  }

  @Test
  public void shouldInsertChannelGroupOnLastPositionWhenPositionsDefined() {
    // given
    int locationId = 123;
    int channelGroupId = 234;
    int channelGroupsCount = 12;

    SuplaChannelGroup suplaChannelGroup =
        suplaChannelGroup(channelGroupId, locationId, "caption", 1, 2, 3, 4);

    Location location = mock(Location.class);
    when(location.getLocationId()).thenReturn(locationId);
    when(locationDao.getLocation(locationId)).thenReturn(location);

    when(channelDao.getChannelGroupLastPositionInLocation(locationId))
        .thenReturn(channelGroupsCount);

    // when
    boolean result = defaultChannelRepository.updateChannelGroup(suplaChannelGroup);

    // then
    assertTrue(result);
    ArgumentCaptor<ChannelGroup> channelGroupArgumentCaptor =
        ArgumentCaptor.forClass(ChannelGroup.class);
    verify(channelDao).getChannelGroup(channelGroupId);
    verify(channelDao, times(2)).getCachedProfileId();
    verify(channelDao).getChannelGroupLastPositionInLocation(locationId);
    verify(channelDao).insert(channelGroupArgumentCaptor.capture());
    verify(locationDao).getLocation(locationId);
    verifyNoMoreInteractions(locationDao, channelDao);

    assertChannelGroup(
        channelGroupArgumentCaptor.getValue(),
        channelGroupId,
        locationId,
        "caption",
        1,
        2,
        3,
        4,
        channelGroupsCount + 1);
  }

  @Test
  public void shouldUpdateChannelGroupWhenFound() {
    // given
    int locationId = 123;
    int channelGroupId = 234;
    Long channelGroupDbId = 345L;

    SuplaChannelGroup suplaChannelGroup =
        suplaChannelGroup(channelGroupId, locationId, "caption", 1, 2, 3, 4);

    Location location = mock(Location.class);
    when(location.getLocationId()).thenReturn(locationId);
    when(locationDao.getLocation(locationId)).thenReturn(location);

    ChannelGroup channelGroup = new ChannelGroup();
    channelGroup.setId(channelGroupDbId);
    when(channelDao.getChannelGroup(channelGroupId)).thenReturn(channelGroup);

    when(channelDao.getChannelGroupLastPositionInLocation(locationId))
        .thenThrow(new NoSuchElementException());

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
    assertChannelGroup(channelGroup, channelGroupId, locationId, "caption", 1, 2, 3, 4, 0);
  }

  @Test
  public void shouldUpdateChannelGroupAndChangePositionWhenMovedToAnotherLocation() {
    // given
    int locationId = 123;
    int channelGroupId = 234;
    Long channelGroupDbId = 345L;

    SuplaChannelGroup suplaChannelGroup =
        suplaChannelGroup(channelGroupId, locationId, "caption1", 9, 8, 7, 6);

    Location location = mock(Location.class);
    when(location.getLocationId()).thenReturn(locationId);
    when(locationDao.getLocation(locationId)).thenReturn(location);

    ChannelGroup channelGroup =
        channelGroup(channelGroupId, locationId + 1, "caption1", 9, 8, 7, 6);
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
    assertChannelGroup(channelGroup, channelGroupId, locationId, "caption1", 9, 8, 7, 6, 0);
  }

  @Test
  public void shouldNotChangeChannelGroupWhenNothingChanged() {
    // given
    int locationId = 123;
    int channelGroupId = 234;
    Long channelGroupDbId = 345L;

    SuplaChannelGroup suplaChannelGroup =
        suplaChannelGroup(channelGroupId, locationId, "caption", 9, 8, 7, 6);

    Location location = mock(Location.class);
    when(locationDao.getLocation(locationId)).thenReturn(location);

    ChannelGroup channelGroup = channelGroup(channelGroupId, locationId, "caption", 9, 8, 7, 6);
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
  public void shouldInsertChannelExtendedValueWhenChannelExtendedValueNotFound() {
    // given
    int channelId = 234;
    SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);

    // when
    ResultTuple result =
        defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

    // then
    assertEquals(Boolean.TRUE, result.asBoolean(0));
    assertEquals(Boolean.FALSE, result.asBoolean(1));
    ArgumentCaptor<ChannelExtendedValue> argumentCaptor =
        ArgumentCaptor.forClass(ChannelExtendedValue.class);
    verify(channelDao).getChannelExtendedValue(channelId);
    verify(channelDao).insert(argumentCaptor.capture());
    verifyNoMoreInteractions(channelDao);
    verifyNoInteractions(locationDao, dateProvider);

    assertSame(suplaChannelExtendedValue, argumentCaptor.getValue().getExtendedValue());
    assertEquals(channelId, argumentCaptor.getValue().getChannelId());
  }

  @Test
  public void
      shouldInsertChannelExtendedValueWhenChannelExtendedValueNotFoundAndCreateTimestampForTimer() {
    // given
    int channelId = 234;
    SuplaTimerState suplaTimerState = mock(SuplaTimerState.class);
    when(suplaTimerState.getCountdownEndsAt()).thenReturn(new Date());
    SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);
    suplaChannelExtendedValue.TimerStateValue = suplaTimerState;
    when(dateProvider.currentTimestamp()).thenReturn(123L);

    // when
    ResultTuple result =
        defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

    // then
    assertEquals(Boolean.TRUE, result.asBoolean(0));
    assertEquals(Boolean.TRUE, result.asBoolean(1));
    ArgumentCaptor<ChannelExtendedValue> argumentCaptor =
        ArgumentCaptor.forClass(ChannelExtendedValue.class);
    verify(channelDao).getChannelExtendedValue(channelId);
    verify(channelDao).insert(argumentCaptor.capture());
    verify(dateProvider).currentTimestamp();
    verifyNoMoreInteractions(channelDao, dateProvider);
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
    when(channelExtendedValue.getTimerStartTimestamp()).thenReturn(null);
    when(channelDao.getChannelExtendedValue(channelId)).thenReturn(channelExtendedValue);

    // when
    ResultTuple result =
        defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

    // then
    assertEquals(Boolean.TRUE, result.asBoolean(0));
    assertEquals(Boolean.FALSE, result.asBoolean(1));
    verify(channelDao).getChannelExtendedValue(channelId);
    verify(channelDao).update(channelExtendedValue);
    verify(channelExtendedValue).setExtendedValue(suplaChannelExtendedValue);
    verify(channelExtendedValue).getTimerStartTimestamp();
    verify(channelExtendedValue, times(2)).getTimerEstimatedEndDate();
    verifyNoMoreInteractions(channelDao, channelExtendedValue);
    verifyNoInteractions(locationDao);
  }

  @Test
  public void shouldUpdateChannelExtendedValueAndCleanupTimerStartTimestamp() {
    // given
    int channelId = 234;
    Date date = new Date();
    SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);

    ChannelExtendedValue channelExtendedValue = mock(ChannelExtendedValue.class);
    when(channelExtendedValue.getTimerStartTimestamp()).thenReturn(1L);
    when(channelExtendedValue.getTimerEstimatedEndDate()).thenReturn(date, (Date) null);
    when(channelDao.getChannelExtendedValue(channelId)).thenReturn(channelExtendedValue);

    // when
    ResultTuple result =
        defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

    // then
    assertEquals(Boolean.TRUE, result.asBoolean(0));
    assertEquals(Boolean.TRUE, result.asBoolean(1));
    verify(channelDao).getChannelExtendedValue(channelId);
    verify(channelDao).update(channelExtendedValue);
    verify(channelExtendedValue).setExtendedValue(suplaChannelExtendedValue);
    verify(channelExtendedValue).getTimerStartTimestamp();
    verify(channelExtendedValue, times(2)).getTimerEstimatedEndDate();
    verify(channelExtendedValue).setTimerStartTimestamp(null);
    verifyNoMoreInteractions(channelDao, channelExtendedValue);
    verifyNoInteractions(locationDao);
  }

  @Test
  public void shouldUpdateChannelExtendedValueAndSetTimerToCurrentDate() {
    // given
    long currentTimestamp = 123L;
    Date date = new Date();
    when(dateProvider.currentTimestamp()).thenReturn(currentTimestamp);

    int channelId = 234;
    SuplaChannelExtendedValue suplaChannelExtendedValue = mock(SuplaChannelExtendedValue.class);

    ChannelExtendedValue channelExtendedValue = mock(ChannelExtendedValue.class);
    when(channelExtendedValue.getTimerEstimatedEndDate()).thenReturn(null, date);
    when(channelDao.getChannelExtendedValue(channelId)).thenReturn(channelExtendedValue);

    // when
    ResultTuple result =
        defaultChannelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);

    // then
    assertEquals(Boolean.TRUE, result.asBoolean(0));
    assertEquals(Boolean.TRUE, result.asBoolean(1));
    verify(channelDao).getChannelExtendedValue(channelId);
    verify(channelDao).update(channelExtendedValue);
    verify(channelExtendedValue).setExtendedValue(suplaChannelExtendedValue);
    verify(channelExtendedValue, times(2)).getTimerEstimatedEndDate();
    verify(channelExtendedValue).setTimerStartTimestamp(currentTimestamp);
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
    ArgumentCaptor<ChannelGroupRelation> argumentCaptor =
        ArgumentCaptor.forClass(ChannelGroupRelation.class);
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
    when(channelDao.setChannelGroupRelationsVisible(visible, whereVisible))
        .thenReturn(expectedResult);

    // when
    boolean result =
        defaultChannelRepository.setChannelGroupRelationsVisible(visible, whereVisible);

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
    // when
    defaultChannelRepository.updateLocation((Location) null);

    // then
    verifyNoInteractions(locationDao, channelDao);
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

  @Test
  public void shouldGetAllChannelsForProfile() {
    // given
    long profileId = 123L;
    Cursor cursor = mock(Cursor.class);
    when(channelDao.getAllChannels("func <> 0  AND (C.profileid = " + profileId + ") "))
        .thenReturn(cursor);

    // when
    Cursor returned = defaultChannelRepository.getAllProfileChannels(profileId);

    // then
    assertSame(cursor, returned);
  }

  @Test
  public void shouldGetAllGroupsForProfile() {
    // given
    long profileId = 123L;
    Cursor cursor = mock(Cursor.class);
    when(channelDao.getAllChannelGroupsForProfileId(profileId)).thenReturn(cursor);

    // when
    Cursor returned = defaultChannelRepository.getAllProfileChannelGroups(profileId);

    // then
    assertSame(cursor, returned);
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
