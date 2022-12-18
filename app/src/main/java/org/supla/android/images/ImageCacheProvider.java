package org.supla.android.images;

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

/**
 * Class created to encapsulate static calls and make code testable. Both method are using {@ling ImageCache} to cache given images.
 */
public class ImageCacheProvider {
    public void addImage(int id, UserIconDao.Image image) {
        ImageCache.addImage(new ImageId(id, image.subId, image.profileId), image.value);
    }

    public void addImage(ImageId imgId, byte[] image) {
        ImageCache.addImage(imgId, image);
    }
}
