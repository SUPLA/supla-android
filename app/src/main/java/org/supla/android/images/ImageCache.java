package org.supla.android.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        return map.containsKey(imgId);
    }

}
