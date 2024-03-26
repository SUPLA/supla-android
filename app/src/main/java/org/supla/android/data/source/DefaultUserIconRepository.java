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

import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.data.source.local.entity.UserIconEntity;
import org.supla.android.db.ProfileIdProvider;
import org.supla.android.images.ImageCacheProxy;

public class DefaultUserIconRepository implements UserIconRepository {

  private final UserIconDao userIconDao;
  private final ImageCacheProxy imageCacheProxy;
  private final ProfileIdProvider profileIdProvider;

  public DefaultUserIconRepository(
      UserIconDao userIconDao,
      ImageCacheProxy imageCacheProxy,
      ProfileIdProvider profileIdProvider) {
    this.userIconDao = userIconDao;
    this.imageCacheProxy = imageCacheProxy;
    this.profileIdProvider = profileIdProvider;
  }

  @Override
  public boolean addUserIcons(int id, byte[] img1, byte[] img2, byte[] img3, byte[] img4) {
    if (id <= 0 || (img1 == null && img2 == null && img3 == null && img4 == null)) {
      return false;
    }

    UserIconDao.Image[] images = {
      new UserIconDao.Image(
          UserIconEntity.COLUMN_IMAGE_1, img1, 1, profileIdProvider.getCachedProfileId()),
      new UserIconDao.Image(
          UserIconEntity.COLUMN_IMAGE_2, img2, 2, profileIdProvider.getCachedProfileId()),
      new UserIconDao.Image(
          UserIconEntity.COLUMN_IMAGE_3, img3, 3, profileIdProvider.getCachedProfileId()),
      new UserIconDao.Image(
          UserIconEntity.COLUMN_IMAGE_4, img4, 4, profileIdProvider.getCachedProfileId()),
    };

    userIconDao.insert(id, images);
    for (UserIconDao.Image image : images) {
      if (image.value != null) {
        imageCacheProxy.addUserImage(id, image);
      }
    }
    return true;
  }
}
