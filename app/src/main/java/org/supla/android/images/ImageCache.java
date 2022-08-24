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
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;

import org.supla.android.SuplaApp;

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
            } else {
                Drawable drw = ContextCompat.getDrawable(context, imgId.getId());
                if(drw != null) {
                    Bitmap bmp = Bitmap.createBitmap(drw.getIntrinsicWidth(),
                                                     drw.getIntrinsicHeight(),
                                                     Bitmap.Config.ARGB_8888);
                    
                    Canvas canvas = new Canvas(bmp);
                    drw.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drw.draw(canvas);
                    result = bmp;
                    map.put(imgId, result);
                }
            }
        }

        return result;
    }

    @BindingAdapter("suplaImage")
    public static void bindBitmap(AppCompatImageView iv, ImageId imgid) {
        android.util.Log.d("Scene", "bind Bitmap called " + imgid.getId());
        Bitmap bmp = ImageCache.getBitmap(SuplaApp.getApp(), imgid);
        android.util.Log.d("Scene", "setting bitmap: " + bmp);
        iv.setImageBitmap(bmp);
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
