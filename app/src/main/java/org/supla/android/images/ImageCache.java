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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;

public class ImageCache {

  private static final LinkedHashMap<ImageId, Bitmap> map = new LinkedHashMap<>();

  public static synchronized Bitmap getBitmap(Context context, ImageId imgId) {
    if (imgId == null) {
      return null;
    }

    Configuration configuration = context.getResources().getConfiguration();
    boolean nightMode =
        (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
    imgId.setNightMode(nightMode);

    if (imgId.getUserImage() && nightMode && !map.containsKey(imgId)) {
      // If there is no user image for night mode, use the default
      imgId.setNightMode(false);
    }

    Bitmap result = map.get(imgId);
    if (result == null && !imgId.getUserImage()) {
      result = BitmapFactory.decodeResource(context.getResources(), imgId.getId());
      if (result != null) {
        map.put(imgId, result);
      } else {
        Drawable drw = ContextCompat.getDrawable(context, imgId.getId());
        if (drw != null) {
          Bitmap bmp =
              Bitmap.createBitmap(
                  drw.getIntrinsicWidth(), drw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

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

  public static Bitmap getUserImageBitmap(Context context, ImageId imgId) {
    if (imgId == null || !imgId.getUserImage()) {
      return null;
    }

    Configuration configuration = context.getResources().getConfiguration();
    boolean nightMode =
        (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
    imgId.setNightMode(nightMode);

    if (imgId.getUserImage() && nightMode && !map.containsKey(imgId)) {
      // If there is no user image for night mode, use the default
      imgId.setNightMode(false);
    }

    return map.get(imgId);
  }

  public static synchronized void loadBitmapForWidgetView(
      ImageId imgId, RemoteViews view, int viewId, boolean nightMode) {
    if (imgId == null) {
      return;
    }

    imgId.setNightMode(nightMode);

    if (imgId.getUserImage()) {
      if (nightMode && !map.containsKey(imgId)) {
        // If there is no user image for night mode, use the default
        imgId.setNightMode(false);
      }

      Bitmap result = map.get(imgId);
      if (result != null) {
        view.setImageViewBitmap(viewId, result);
      }
    } else {
      view.setImageViewResource(viewId, imgId.getId());
    }
  }

  public static synchronized boolean bitmapExists(ImageId imgId) {
    return imgId != null && map.containsKey(imgId);
  }

  public static synchronized int size() {
    return map.size();
  }

  public static synchronized int sum() {
    return map.keySet().hashCode();
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
