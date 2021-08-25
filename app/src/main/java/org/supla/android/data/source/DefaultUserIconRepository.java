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

import android.database.Cursor;

import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.db.SuplaContract;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageCacheProvider;
import org.supla.android.images.ImageId;

public class DefaultUserIconRepository implements UserIconRepository {

    private final UserIconDao userIconDao;
    private final ImageCacheProvider imageCacheProvider;
    private final int profileId;

    public DefaultUserIconRepository(UserIconDao userIconDao,
                                     ImageCacheProvider imageCacheProvider,
                                     int profileId) {
        this.userIconDao = userIconDao;
        this.imageCacheProvider = imageCacheProvider;
        this.profileId = profileId;
    }

    @Override
    public boolean addUserIcons(int id, byte[] img1, byte[] img2, byte[] img3, byte[] img4) {
        if (id <= 0 || (img1 == null && img2 == null && img3 == null && img4 == null)) {
            return false;
        }

        UserIconDao.Image[] images = {
                new UserIconDao.Image(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1, img1, 1),
                new UserIconDao.Image(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2, img2, 2),
                new UserIconDao.Image(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3, img3, 3),
                new UserIconDao.Image(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4, img4, 4),
        };

        userIconDao.insert(id, images);
        for (UserIconDao.Image image : images) {
            if (image.value != null) {
                imageCacheProvider.addImage(id, image);
            }
        }
        return true;
    }

    @Override
    public void deleteUserIcons() {
        userIconDao.delete();
    }

    @Override
    public void loadUserIconsIntoCache() {
        Cursor cursor = userIconDao.getUserIcons();
        if (cursor.moveToFirst()) {
            do {
                for (ImageType imageType : ImageType.values()) {
                    byte[] image = cursor.getBlob(cursor.getColumnIndex(imageType.column));
                    int remoteId = cursor.getInt(cursor.getColumnIndex(
                            SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID));

                    if (image != null && image.length > 0) {
                        imageCacheProvider.addImage(new ImageId(remoteId, imageType.subId), image);
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private enum ImageType {
        IMAGE1(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1, 1),
        IMAGE2(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2, 2),
        IMAGE3(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3, 3),
        IMAGE4(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4, 4);

        private final String column;
        private final int subId;

        ImageType(String column, int subId) {
            this.column = column;
            this.subId = subId;
        }
    }
}
