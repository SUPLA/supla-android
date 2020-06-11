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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;

public class ImageCache {
    private static final LinkedHashMap<ImageId, Bitmap> map = new LinkedHashMap<>();

    public static synchronized Bitmap getBitmap(Context context, ImageId imgId) {
        if (imgId == null) {
            return null;
        }

        Bitmap result = map.get(imgId);
        if (result == null && !imgId.isUserImage()) {
            result = BitmapFactory.decodeResource(context.getResources(), imgId.getId());
            if (result != null) {
                map.put(imgId, result);
            }
        }

        return result;
    }

    public static synchronized boolean bitmapExists(ImageId imgId) {
        return imgId != null && map.containsKey(imgId);
    }

    public static synchronized boolean addBitmap(ImageId imgId, Bitmap bmp) {
        if (!bitmapExists(imgId)) {
            return map.put(imgId, bmp) != null;
        }

        return false;
    }

    public static synchronized boolean addImage(ImageId imgId, byte[] image) {
        if (imgId == null || image == null) {
            return false;
        }

        ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
        return addBitmap(imgId, BitmapFactory.decodeStream(imageStream));
    }

    public static synchronized void clear() {
        map.clear();
    }
}
