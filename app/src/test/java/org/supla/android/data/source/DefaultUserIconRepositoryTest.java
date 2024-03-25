package org.supla.android.data.source;

/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.data.source.local.entity.UserIconEntity;
import org.supla.android.db.ProfileIdProvider;
import org.supla.android.images.ImageCacheProxy;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUserIconRepositoryTest {

  @Mock private UserIconDao userIconDao;
  @Mock private ImageCacheProxy imageCacheProxy;
  @Mock private ProfileIdProvider profileIdProvider;

  @InjectMocks private DefaultUserIconRepository userIconRepository;

  @Test
  public void shouldNotAddUserIconsWhenWrongId() {
    // given
    int id = -2;
    byte[] img1 = new byte[0];
    byte[] img2 = new byte[0];
    byte[] img3 = new byte[0];
    byte[] img4 = new byte[0];

    // when
    boolean result = userIconRepository.addUserIcons(id, img1, img2, img3, img4);

    // then
    assertFalse(result);
    verifyNoInteractions(userIconDao, imageCacheProxy);
  }

  @Test
  public void shouldNotAddUserIconsWhenAllImageAreNull() {
    // given
    int id = 5;

    // when
    boolean result = userIconRepository.addUserIcons(id, null, null, null, null);

    // then
    assertFalse(result);
    verifyNoInteractions(userIconDao, imageCacheProxy);
  }

  @Test
  public void shouldAddUserIcons() {
    // given
    int id = 5;
    byte[] img1 = new byte[0];
    byte[] img2 = new byte[0];
    byte[] img3 = new byte[0];
    byte[] img4 = new byte[0];

    // when
    boolean result = userIconRepository.addUserIcons(id, img1, img2, img3, img4);

    // then
    assertTrue(result);

    ArgumentCaptor<UserIconDao.Image[]> insertedImagesCaptor =
        ArgumentCaptor.forClass(UserIconDao.Image[].class);
    verify(userIconDao).insert(eq(id), insertedImagesCaptor.capture());
    ArgumentCaptor<UserIconDao.Image> cachedImagesCaptor =
        ArgumentCaptor.forClass(UserIconDao.Image.class);
    verify(imageCacheProxy, times(4)).addUserImage(eq(id), cachedImagesCaptor.capture());
    verifyNoMoreInteractions(userIconDao, imageCacheProxy);

    UserIconDao.Image[] insertedImages = insertedImagesCaptor.getValue();
    assertEquals(4, insertedImages.length);
    assertImage(insertedImages[0], UserIconEntity.COLUMN_IMAGE_1, img1, 1);
    assertImage(insertedImages[1], UserIconEntity.COLUMN_IMAGE_2, img2, 2);
    assertImage(insertedImages[2], UserIconEntity.COLUMN_IMAGE_3, img3, 3);
    assertImage(insertedImages[3], UserIconEntity.COLUMN_IMAGE_4, img4, 4);

    List<UserIconDao.Image> cachedImages = cachedImagesCaptor.getAllValues();
    assertEquals(4, cachedImages.size());
    assertImage(cachedImages.get(0), UserIconEntity.COLUMN_IMAGE_1, img1, 1);
    assertImage(cachedImages.get(1), UserIconEntity.COLUMN_IMAGE_2, img2, 2);
    assertImage(cachedImages.get(2), UserIconEntity.COLUMN_IMAGE_3, img3, 3);
    assertImage(cachedImages.get(3), UserIconEntity.COLUMN_IMAGE_4, img4, 4);
  }

  private void assertImage(UserIconDao.Image image, String column, byte[] value, int subId) {
    assertEquals(column, image.column);
    assertSame(value, image.value);
    assertEquals(subId, image.subId);
  }
}
