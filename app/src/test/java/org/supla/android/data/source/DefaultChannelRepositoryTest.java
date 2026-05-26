package org.supla.android.data.source;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.db.Location;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultChannelRepositoryTest {

  @Mock private ChannelDao channelDao;
  @Mock private LocationDao locationDao;

  @InjectMocks private DefaultChannelRepository defaultChannelRepository;

  @Test
  public void shouldReorderChannelGroups() {
    // given
    int locationId = 2;
    long profileId = 1;
    String locationCaption = "Caption";

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(anyInt())).thenReturn(15L, 12L, 18L, 13L, 14L);
    when(cursor.moveToNext()).thenReturn(true, true, true, true, false);
    when(channelDao.getSortedChannelGroupIdsForLocationCursor(locationCaption)).thenReturn(cursor);

    Location location = mock(Location.class);
    when(location.getCaption()).thenReturn(locationCaption);
    when(locationDao.getLocation(locationId, profileId)).thenReturn(location);

    // when
    defaultChannelRepository.reorderChannelGroups(15L, locationId, 13L, profileId).blockingAwait();

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
}
